package com.example.aplikacja_sieciowa.data.remote

import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.data.model.IRCCommand
import com.example.aplikacja_sieciowa.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IRCSocketClient @Inject constructor() {

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    private var listenerJob: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _currentNickname = MutableStateFlow<String?>(null)
    val currentNickname: StateFlow<String?> = _currentNickname.asStateFlow()

    private val _serverResponses = MutableStateFlow<List<String>>(emptyList())
    val serverResponses: StateFlow<List<String>> = _serverResponses.asStateFlow()

    private val _channelUsers = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val channelUsers: StateFlow<Map<String, List<String>>> = _channelUsers.asStateFlow()

    private var lastRequestedChannel: String? = null

    suspend fun connect(host: String, port: Int) {
        withContext(Dispatchers.IO) {
            try {
                _connectionState.value = ConnectionState.Connecting

                socket = Socket(host, port)
                writer = BufferedWriter(OutputStreamWriter(socket?.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket?.getInputStream()))

                _connectionState.value = ConnectionState.Connected

                startListening()

            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Błąd połączenia: ${e.message}")
                disconnect()
            }
        }
    }

    private fun startListening() {
        listenerJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (socket?.isConnected == true) {
                    val line = reader?.readLine() ?: break
                    parseServerMessage(line)
                }
            } catch (e: Exception) {
                if (_connectionState.value is ConnectionState.Connected) {
                    _connectionState.value = ConnectionState.Error("Utracono połączenie: ${e.message}")
                }
            } finally {
                disconnect()
            }
        }
    }

    private fun parseServerMessage(line: String) {
        addServerResponse(line)

        val parts = line.split(" ", limit = 4)
        if (parts.isEmpty()) return

        when (parts[0]) {
            "OK" -> {
                if (parts.size >= 2 && parts[1] == "NICK" && parts.size >= 3) {
                    _currentNickname.value = parts[2]
                }
            }

            "MESSAGE" -> {
                if (parts.size >= 4) {
                    val channel = parts[1]
                    val sender = parts[2]
                    val content = parts[3]

                    val message = Message(
                        channel = channel,
                        sender = sender,
                        content = content,
                        isPrivate = false
                    )
                    _messages.value = _messages.value + message
                }
            }

            "PRIVMSG" -> {
                if (parts.size >= 3) {
                    val sender = parts[1]
                    val content = parts[2]

                    val message = Message(
                        channel = "PRIVATE",
                        sender = sender,
                        content = content,
                        isPrivate = true
                    )
                    _messages.value = _messages.value + message
                }
            }

            "USERJOINED" -> {
                if (parts.size >= 3) {
                    val channel = parts[1]
                    val nickname = parts[2]

                    val systemMsg = Message(
                        channel = channel,
                        sender = "SYSTEM",
                        content = "$nickname dołączył do kanału"
                    )
                    _messages.value = _messages.value + systemMsg
                }
            }

            "USERLEFT" -> {
                if (parts.size >= 3) {
                    val channel = parts[1]
                    val nickname = parts[2]

                    val systemMsg = Message(
                        channel = channel,
                        sender = "SYSTEM",
                        content = "$nickname opuścił kanał"
                    )
                    _messages.value = _messages.value + systemMsg
                }
            }

            "CHANNELLIST" -> {
                if (parts.size >= 2) {
                    val channelNames = parts[1].split(",").filter { it.isNotBlank() }
                    _channels.value = channelNames.map { Channel(name = it) }
                }
            }

            "USERLIST" -> {

                if (parts.size >= 2) {
                    val users = parts[1].split(",").filter { it.isNotBlank() }
                    lastRequestedChannel?.let { channel ->
                        _channelUsers.value = _channelUsers.value + (channel to users)

                    }
                }
            }


                "ERROR" -> {
                val errorMsg = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Unknown error"
                val systemMsg = Message(
                    channel = "SYSTEM",
                    sender = "ERROR",
                    content = errorMsg
                )
                _messages.value = _messages.value + systemMsg
            }
        }
    }

    private fun addServerResponse(response: String) {
        _serverResponses.value = (_serverResponses.value + response).takeLast(50)
    }

    suspend fun sendCommand(command: IRCCommand) {
        withContext(Dispatchers.IO) {
            try {
                if (command is IRCCommand.Users) {
                    lastRequestedChannel = command.channel
                }

                writer?.write(command.commandText + "\n")
                writer?.flush()
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Błąd wysyłania: ${e.message}")
            }
        }
    }

    fun disconnect() {
        listenerJob?.cancel()
        runCatching {
            writer?.close()
            reader?.close()
            socket?.close()
        }
        socket = null
        writer = null
        reader = null
        _connectionState.value = ConnectionState.Disconnected
        _currentNickname.value = null

        _messages.value = emptyList()
        _channels.value = emptyList()
        _channelUsers.value = emptyMap()
        _serverResponses.value = emptyList()
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun addLocalMessage(message: Message) {
        _messages.value = _messages.value + message
    }
}