package com.example.aplikacja_sieciowa.domain.repository

import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.data.model.Message
import kotlinx.coroutines.flow.StateFlow

interface IRCRepository {
    val connectionState: StateFlow<ConnectionState>
    val messages: StateFlow<List<Message>>
    val channels: StateFlow<List<Channel>>
    val currentNickname: StateFlow<String?>
    val serverResponses: StateFlow<List<String>>

    suspend fun connect(host: String, port: Int)
    suspend fun setNickname(nickname: String)
    suspend fun joinChannel(channel: String)
    suspend fun leaveChannel(channel: String)
    suspend fun sendMessage(channel: String, text: String)
    suspend fun sendPrivateMessage(recipient: String, text: String)
    suspend fun requestChannelList()
    suspend fun requestUsers(channel: String)
    fun disconnect()
    fun clearMessages()
    fun addLocalMessage(message: Message)

}