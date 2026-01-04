package com.example.aplikacja_sieciowa.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.Message
import com.example.aplikacja_sieciowa.domain.repository.IRCRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: IRCRepository
): ViewModel() {

    val channels: StateFlow<List<Channel>> = repository.channels
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val messages: StateFlow<List<Message>> = repository.messages
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentNickname: StateFlow<String?> = repository.currentNickname
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _currentChannel = MutableStateFlow<String?>(null)
    val currentChannel: StateFlow<String?> = _currentChannel.asStateFlow()

    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    private val _userListForChannel = MutableStateFlow<List<String>>(emptyList())
    val userListForChannel: StateFlow<List<String>> = _userListForChannel.asStateFlow()

    val messagesForCurrentChannel: StateFlow<List<Message>> = messages.combine(currentChannel) {msgs, channel ->
        if (channel == null) emptyList()
        else msgs.filter { it.channel == channel }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    init{
        viewModelScope.launch {
            repository.requestChannelList()
        }
    }

    fun updateMessageText(text: String) {
        _messageText.value = text
    }

    fun joinChannel(channelName: String) {
        viewModelScope.launch{
            repository.joinChannel(channelName)
            _currentChannel.value = channelName
            repository.requestUsers(channelName)
        }
    }

    fun leaveChannel(channelName: String) {
        viewModelScope.launch {
            repository.leaveChannel(channelName)
            if(_currentChannel.value == channelName) {
                _currentChannel.value = null
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

    fun sendMessage() {
        val channel = _currentChannel.value
        val text = _messageText.value
        val nickname = currentNickname.value

        if (channel != null && text.isNotBlank() && nickname != null) {
            viewModelScope.launch {
                // Dodaj wiadomość lokalnie (optymistyczne UI)
                val localMessage = Message(
                    channel = channel,
                    sender = nickname,
                    content = text,
                    timestamp = System.currentTimeMillis(),
                    isPrivate = false
                )

                // Dodaj do listy messages bezpośrednio
                repository.addLocalMessage(localMessage)

                // Wyślij do serwera
                repository.sendMessage(channel, text)
                _messageText.value = ""
            }
        }
    }

    fun refreshChannel() {
        viewModelScope.launch {
            repository.requestChannelList()
        }
    }

    fun refreshUsers() {
        _currentChannel.value?.let { channel ->
            viewModelScope.launch {
                repository.requestUsers(channel)
            }
        }
    }

    fun disconnect(onDisconnected: () -> Unit) {
        repository.disconnect()
        onDisconnected()
    }













}