package com.example.aplikacja_sieciowa.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.data.model.ConversationType
import com.example.aplikacja_sieciowa.data.model.Message
import com.example.aplikacja_sieciowa.domain.repository.IRCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: IRCRepository
) : ViewModel() {

    val channels: StateFlow<List<Channel>> = repository.channels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<Message>> = repository.messages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentNickname: StateFlow<String?> = repository.currentNickname
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val channelUsers: StateFlow<Map<String, List<String>>> = repository.channelUsers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.Connected
        )

    private val _currentConversation = MutableStateFlow<ConversationType?>(null)
    val currentConversation: StateFlow<ConversationType?> = _currentConversation.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    val directMessageConversations: StateFlow<List<String>> = messages
        .map { allMessages ->
            allMessages
                .filter { it.isPrivate }
                .map { it.sender }
                .distinct()
                .filter { it != currentNickname.value }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messagesForCurrentConversation: StateFlow<List<Message>> = combine(
        messages,
        currentConversation,
        currentNickname
    ) { msgs, conversation, myNickname ->
        when (conversation) {
            is ConversationType.ChannelConversation -> {
                msgs.filter { it.channel == conversation.channelName }
            }
            is ConversationType.DirectMessage -> {
                msgs.filter { msg ->
                    msg.isPrivate && (
                            (msg.sender == conversation.username) ||
                                    (msg.sender == myNickname && msg.channel == "DM_${conversation.username}")
                            )
                }
            }
            null -> emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentChannelUsers: StateFlow<List<String>> = combine(
        currentConversation,
        channelUsers
    ) { conversation, usersMap ->
        if (conversation is ConversationType.ChannelConversation) {
            usersMap[conversation.channelName] ?: emptyList()
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.requestChannelList()
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun joinChannel(channelName: String) {
        viewModelScope.launch {
            repository.joinChannel(channelName)
            _currentConversation.value = ConversationType.ChannelConversation(channelName)
            repository.requestUsers(channelName)
        }
    }

    fun openDirectMessage(username: String) {
        _currentConversation.value = ConversationType.DirectMessage(username)
    }

    fun leaveChannel(channelName: String) {
        viewModelScope.launch {
            repository.leaveChannel(channelName)
            val current = _currentConversation.value
            if (current is ConversationType.ChannelConversation && current.channelName == channelName) {
                _currentConversation.value = null
            }
        }
    }

    fun sendMessage() {
        val conversation = _currentConversation.value
        val text = _messageText.value
        val nickname = currentNickname.value

        if (text.isNotBlank() && nickname != null) {
            viewModelScope.launch {
                when (conversation) {
                    is ConversationType.ChannelConversation -> {
                        val localMessage = Message(
                            channel = conversation.channelName,
                            sender = nickname,
                            content = text,
                            timestamp = System.currentTimeMillis(),
                            isPrivate = false
                        )
                        repository.addLocalMessage(localMessage)
                        repository.sendMessage(conversation.channelName, text)
                    }
                    is ConversationType.DirectMessage -> {
                        val localMessage = Message(
                            channel = "DM_${conversation.username}",
                            sender = nickname,
                            content = text,
                            timestamp = System.currentTimeMillis(),
                            isPrivate = true
                        )
                        repository.addLocalMessage(localMessage)
                        repository.sendPrivateMessage(conversation.username, text)
                    }
                    null -> return@launch
                }
                _messageText.value = ""
            }
        }
    }

    fun createAndJoinChannel(channelName: String) {
        var name = channelName.trim()
        if (!name.startsWith("#")) {
            name = "#$name"
        }
        if (name.length > 1) {
            joinChannel(name)
        }
    }

    fun refreshChannels() {
        viewModelScope.launch {
            repository.requestChannelList()
        }
    }

    fun refreshUsers() {
        val conversation = _currentConversation.value
        if (conversation is ConversationType.ChannelConversation) {
            viewModelScope.launch {
                repository.requestUsers(conversation.channelName)
            }
        }
    }

    fun disconnect(onDisconnected: () -> Unit) {
        repository.disconnect()
        onDisconnected()
    }
}