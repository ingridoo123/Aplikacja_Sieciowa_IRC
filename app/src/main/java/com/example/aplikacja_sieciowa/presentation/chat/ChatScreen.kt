package com.example.aplikacja_sieciowa.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aplikacja_sieciowa.R
import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.Message
import com.example.aplikacja_sieciowa.ui.theme.DiscordBlurple
import com.example.aplikacja_sieciowa.ui.theme.DiscordChannelSelected
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkerBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordDarkestBackground
import com.example.aplikacja_sieciowa.ui.theme.DiscordRed
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextMuted
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextPrimary
import com.example.aplikacja_sieciowa.ui.theme.DiscordTextSecondary
import com.example.aplikacja_sieciowa.ui.theme.DiscordYellow
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onDisconnect: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val channels by viewModel.channels.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val currentNickname by viewModel.currentNickname.collectAsState()


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChannelsDrawer(
                channels = channels,
                currentChannel = currentChannel,
                currentNickname = currentNickname,
                onChannelClick = { channel ->
                    viewModel.joinChannel(channel)
                    scope.launch { drawerState.close() }
                },
                onRefresh = {viewModel.refreshChannel()},
                onDisconnect = {
                    viewModel.disconnect(onDisconnect)
                },
                onCreateChannel = {channelName ->
                    viewModel.createAndJoinChannel(channelName)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                ChatTopBar(
                    currentChannel = currentChannel,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onUsersClick = {
                        // TODO: Right drawer for users
                    }
                )
            },
            containerColor = DiscordDarkBackground
        ) { padding ->
            if(currentChannel == null) {
                NoChannelSelected(
                    modifier = Modifier.padding(padding),
                    onOpenChannels = {
                        scope.launch { drawerState.open() }
                    }
                )
            } else {
                ChatContent(
                    messages = messages,
                    messageText = messageText,
                    onMessageTextChange = {viewModel.updateMessageText(it)},
                    onSendMessage = { viewModel.sendMessage() },
                    modifier = Modifier.padding(padding)
                )
            }

        }
    }


}


@Composable
fun NoChannelSelected(
    modifier: Modifier = Modifier,
    onOpenChannels: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painterResource(id = R.drawable.question_mark),
                contentDescription = null,
                tint = DiscordTextMuted,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Nie wybrano kanału",
                style = MaterialTheme.typography.headlineSmall,
                color = DiscordTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wybierz kanał z menu po lewej",
                style = MaterialTheme.typography.bodyMedium,
                color = DiscordTextSecondary
            )
            Button(
                onClick = onOpenChannels,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiscordBlurple
                )
            ) {
                Text("Otwórz listę kanałów")
            }
        }
    }
}



@Composable
fun ChatContent(
    messages: List<Message>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DiscordDarkBackground)
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageItem(message)
            }
        }

        // Message Input
        MessageInput(
            text = messageText,
            onTextChange = onMessageTextChange,
            onSend = onSendMessage
        )
    }
}


@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(DiscordBlurple),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.sender.firstOrNull()?.uppercase() ?: "?",
                color = DiscordTextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.sender,
                    color = if (message.sender == "SYSTEM") DiscordYellow else DiscordTextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(message.timestamp)),
                    color = DiscordTextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.content,
                color = DiscordTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DiscordDarkerBackground)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Wiadomość...", color = DiscordTextMuted) },
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DiscordDarkestBackground,
                unfocusedContainerColor = DiscordDarkestBackground,
                focusedTextColor = DiscordTextPrimary,
                unfocusedTextColor = DiscordTextSecondary,
                cursorColor = DiscordTextPrimary,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) DiscordBlurple else DiscordTextMuted)
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Wyślij",
                tint = DiscordTextPrimary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    currentChannel: String?,
    onMenuClick: () -> Unit,
    onUsersClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.hashtag),
                    contentDescription = null,
                    tint = DiscordTextMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentChannel?.removePrefix("#") ?: "wybierz kanał",
                    color = DiscordTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = null, tint = DiscordTextPrimary )
            }
        },
        actions = {
            if(currentChannel != null) {
                IconButton(onClick = onUsersClick) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DiscordTextPrimary )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DiscordDarkerBackground)
    )
}



@Composable
fun ChannelsDrawer(
    channels: List<Channel>,
    currentChannel: String?,
    currentNickname: String?,
    onChannelClick: (String) -> Unit,
    onCreateChannel: (String) -> Unit,  // ← NOWY parametr
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newChannelName by remember { mutableStateOf("") }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Utwórz/Dołącz do kanału", color = DiscordTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newChannelName,
                    onValueChange = { newChannelName = it },
                    label = { Text("Nazwa kanału") },
                    placeholder = { Text("np. general") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiscordBlurple,
                        unfocusedBorderColor = DiscordDarkestBackground,
                        focusedLabelColor = DiscordBlurple,
                        unfocusedLabelColor = DiscordTextMuted,
                        focusedTextColor = DiscordTextPrimary,
                        unfocusedTextColor = DiscordTextSecondary,
                        cursorColor = DiscordTextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChannelName.isNotBlank()) {
                            onCreateChannel(newChannelName)
                            newChannelName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DiscordBlurple
                    )
                ) {
                    Text("Dołącz")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Anuluj", color = DiscordTextSecondary)
                }
            },
            containerColor = DiscordDarkerBackground
        )
    }

    ModalDrawerSheet(
        drawerContainerColor = DiscordDarkerBackground,
        modifier = Modifier.width(280.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DiscordDarkestBackground)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "IRC Client",
                        style = MaterialTheme.typography.titleLarge,
                        color = DiscordTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentNickname ?: "Nieznany",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DiscordTextSecondary
                    )
                }
            }

            Divider(color = DiscordDarkestBackground, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KANAŁY TEKSTOWE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiscordTextMuted,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Utwórz kanał",
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Odśwież",
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Channels List
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(channels) { channel ->
                    ChannelItem(
                        channel = channel,
                        isSelected = channel.name == currentChannel,
                        onClick = { onChannelClick(channel.name) }
                    )
                }
            }

            Divider(color = DiscordDarkestBackground, thickness = 1.dp)

            // Disconnect Button
            TextButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DiscordRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rozłącz")
            }
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) DiscordChannelSelected else androidx.compose.ui.graphics.Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.hashtag),
            contentDescription = null,
            tint = if(isSelected) DiscordTextPrimary else DiscordTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = channel.name.removePrefix("#"),
            color = if (isSelected) DiscordTextPrimary else DiscordTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}





