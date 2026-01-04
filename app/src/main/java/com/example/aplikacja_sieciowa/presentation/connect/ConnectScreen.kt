package com.example.aplikacja_sieciowa.presentation.connect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aplikacja_sieciowa.data.model.ConnectionState

@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val serverResponses by viewModel.serverResponses.collectAsState()
    val host by viewModel.host.collectAsState()
    val port by viewModel.port.collectAsState()
    val nickname by viewModel.nickname.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 30.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IRC Client - Test Połączenia",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        ConnectionStatusCard(connectionState)

        Spacer(modifier = Modifier.height(24.dp))

        if (connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
            OutlinedTextField(
                value = host,
                onValueChange = viewModel::updateHost,
                label = { Text("Host") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = port,
                onValueChange = viewModel::updatePort,
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::connect,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Połącz z serwerem")
            }
        }

        if (connectionState is ConnectionState.Connected) {
            OutlinedTextField(
                value = nickname,
                onValueChange = viewModel::updateNickname,
                label = { Text("Pseudonim") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::setNickname,
                modifier = Modifier.fillMaxWidth(),
                enabled = nickname.isNotBlank()
            ) {
                Text("Ustaw pseudonim")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::disconnect,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Rozłącz")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Odpowiedzi serwera:",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                reverseLayout = true
            ) {
                items(serverResponses.reversed()) { response ->
                    Text(
                        text = response,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(state: ConnectionState) {
    val (text, color) = when (state) {
        is ConnectionState.Disconnected -> "Rozłączony" to MaterialTheme.colorScheme.error
        is ConnectionState.Connecting -> "Łączenie..." to MaterialTheme.colorScheme.tertiary
        is ConnectionState.Connected -> "Połączony" to MaterialTheme.colorScheme.primary
        is ConnectionState.Error -> "Błąd: ${state.message}" to MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}