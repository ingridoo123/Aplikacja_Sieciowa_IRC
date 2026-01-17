package com.example.aplikacja_sieciowa.presentation.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aplikacja_sieciowa.data.model.ConnectionState
import com.example.aplikacja_sieciowa.presentation.connect.ConnectViewModel
import com.example.aplikacja_sieciowa.ui.theme.DiscordBlurple
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkerBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkestBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordGreen
import com.example.aplikacja_sieciowa.ui.theme.DiscordRed
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextMuted
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextPrimary
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextSecondary
import com.example.aplikacja_sieciowa.ui.theme.DiscordYellow

@Composable
fun WelcomeScreen(
    onNavigateToChat: () -> Unit,
    viewModel: ConnectViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val serverResponses by viewModel.serverResponses.collectAsState()
    val host by viewModel.host.collectAsState()
    val port by viewModel.port.collectAsState()
    val nickname by viewModel.nickname.collectAsState()
    val currentNickname by viewModel.currentNickname.collectAsState()

    LaunchedEffect(currentNickname) {
        if(currentNickname != null) {
            onNavigateToChat()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DiscordDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(
                containerColor = DiscordDarkerBackground
            ),
            shape = RoundedCornerShape(15.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(DiscordBlurple, RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "IRC",
                        style = MaterialTheme.typography.headlineMedium,
                        color = DiscordTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Witaj z powrotem!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DiscordTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Miło cię widzieć!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiscordTextSecondary,
                )

                Spacer(modifier = Modifier.height(28.dp))

                if(connectionState is ConnectionState.Disconnected || connectionState is ConnectionState.Error) {
                    OutlinedTextField(
                        value = host     ,
                        onValueChange = {viewModel.updateHost(it)},
                        label = { Text("ADRES SERWERA", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDarkestBackground,
                            focusedLabelColor = DiscordBlurple,
                            unfocusedLabelColor = DiscordTextMuted,
                            focusedTextColor = DiscordTextPrimary,
                            unfocusedTextColor = DiscordTextSecondary,
                            cursorColor = DiscordTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )


                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = port,
                        onValueChange = viewModel::updatePort,
                        label = { Text("PORT", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscordBlurple,
                            unfocusedBorderColor = DiscordDarkestBackground,
                            focusedLabelColor = DiscordBlurple,
                            unfocusedLabelColor = DiscordTextMuted,
                            focusedTextColor = DiscordTextPrimary,
                            unfocusedTextColor = DiscordTextSecondary,
                            cursorColor = DiscordTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.connect() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiscordBlurple,
                            contentColor = DiscordTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Połącz",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                // ========== STAN: Connecting ==========
                if (connectionState is ConnectionState.Connecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = DiscordBlurple,
                        strokeWidth = 4.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Łączenie z serwerem...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiscordTextSecondary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.disconnect() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiscordRed,
                            contentColor = DiscordTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Anuluj",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if(connectionState is ConnectionState.Connected && currentNickname == null) {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { viewModel.updateNickname(it) },
                        label = { Text("PSEUDONIM", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DiscordGreen,
                            unfocusedBorderColor = DiscordDarkestBackground,
                            focusedLabelColor = DiscordGreen,
                            unfocusedLabelColor = DiscordTextMuted,
                            focusedTextColor = DiscordTextPrimary,
                            unfocusedTextColor = DiscordTextSecondary,
                            cursorColor = DiscordTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.setNickname() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = nickname.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DiscordGreen,
                            contentColor = DiscordTextPrimary,
                            disabledContainerColor = DiscordTextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Kontynuuj",
                            modifier = Modifier.padding(vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = viewModel::disconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = DiscordBlurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Zmień serwer",
                            color = DiscordTextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusIndicator(state: ConnectionState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when (state) {
                    is ConnectionState.Disconnected -> DiscordDarkestBackground
                    is ConnectionState.Connecting -> DiscordYellow.copy(alpha = 0.2f)
                    is ConnectionState.Connected -> DiscordGreen.copy(alpha = 0.2f)
                    is ConnectionState.Error -> DiscordRed.copy(alpha = 0.2f)
                },
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (state) {
                is ConnectionState.Disconnected -> Icons.Default.Close
                is ConnectionState.Connecting -> Icons.Default.Refresh
                is ConnectionState.Connected -> Icons.Default.Check
                is ConnectionState.Error -> Icons.Default.Close
            },
            contentDescription = null,
            tint = when (state) {
                is ConnectionState.Disconnected -> DiscordTextMuted
                is ConnectionState.Connecting -> DiscordYellow
                is ConnectionState.Connected -> DiscordGreen
                is ConnectionState.Error -> DiscordRed
            },
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when (state) {
                is ConnectionState.Disconnected -> "Rozłączony"
                is ConnectionState.Connecting -> "Łączenie..."
                is ConnectionState.Connected -> "Połączony"
                is ConnectionState.Error -> "Błąd: ${state.message}"
            },
            color = when (state) {
                is ConnectionState.Disconnected -> DiscordTextMuted
                is ConnectionState.Connecting -> DiscordYellow
                is ConnectionState.Connected -> DiscordGreen
                is ConnectionState.Error -> DiscordRed
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}