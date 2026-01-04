package com.example.aplikacja_sieciowa.presentation.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.domain.repository.IRCRepository
import com.example.aplikacja_sieciowa.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val repository: IRCRepository
): ViewModel() {

    val connectionState: StateFlow<ConnectionState> = repository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionState.Disconnected
        )
    val serverResponses = repository.serverResponses
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val currentNickname = repository.currentNickname
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _host = MutableStateFlow(Constants.DEFAULT_SERVER_HOST)
    val host: StateFlow<String> = _host.asStateFlow()

    private val _port = MutableStateFlow(Constants.DEFAULT_SERVER_PORT.toString())
    val port: StateFlow<String> = _port.asStateFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()



    fun updateHost(newHost: String) {
        _host.value = newHost
    }

    fun updatePort(newPort: String) {
        _port.value = newPort
    }

    fun updateNickname(newNickname: String) {
        _nickname.value = newNickname
    }

    fun connect() {
        viewModelScope.launch {
            val portInt = _port.value.toIntOrNull() ?: Constants.DEFAULT_SERVER_PORT
            repository.connect(_host.value, portInt)
        }
    }

    fun setNickname() {
        if (_nickname.value.isNotBlank()) {
            viewModelScope.launch {
                repository.setNickname(_nickname.value)
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
    }



}