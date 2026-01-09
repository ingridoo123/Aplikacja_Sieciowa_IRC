package com.example.aplikacja_sieciowa.data.repository

import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.data.model.IRCCommand
import com.example.aplikacja_sieciowa.data.model.Message
import com.example.aplikacja_sieciowa.data.remote.IRCSocketClient
import com.example.aplikacja_sieciowa.domain.repository.IRCRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IRCRepositoryImpl @Inject constructor(
    private val socketClient: IRCSocketClient
) : IRCRepository {

    override val connectionState: StateFlow<ConnectionState> = socketClient.connectionState
    override val messages: StateFlow<List<Message>> = socketClient.messages
    override val channels: StateFlow<List<Channel>> = socketClient.channels
    override val currentNickname: StateFlow<String?> = socketClient.currentNickname
    override val serverResponses: StateFlow<List<String>> = socketClient.serverResponses
    override val channelUsers: StateFlow<Map<String, List<String>>> = socketClient.channelUsers

    override suspend fun connect(host: String, port: Int) {
        socketClient.connect(host, port)
    }

    override suspend fun setNickname(nickname: String) {
        socketClient.sendCommand(IRCCommand.Nick(nickname))
    }

    override suspend fun joinChannel(channel: String) {
        socketClient.sendCommand(IRCCommand.Join(channel))
    }

    override suspend fun leaveChannel(channel: String) {
        socketClient.sendCommand(IRCCommand.Leave(channel))
    }

    override suspend fun sendMessage(channel: String, text: String) {
        socketClient.sendCommand(IRCCommand.Message(channel, text))
    }

    override suspend fun sendPrivateMessage(recipient: String, text: String) {
        socketClient.sendCommand(IRCCommand.PrivateMessage(recipient, text))
    }

    override suspend fun requestChannelList() {
        socketClient.sendCommand(IRCCommand.List)
    }

    override suspend fun requestUsers(channel: String) {
        socketClient.sendCommand(IRCCommand.Users(channel))
    }

    override fun disconnect() {
        socketClient.disconnect()
    }

    override fun clearMessages() {
        socketClient.clearMessages()
    }

    override fun addLocalMessage(message: Message) {
        socketClient.addLocalMessage(message)
    }
}