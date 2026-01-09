package com.example.aplikacja_sieciowa.presentation.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aplikacja_sieciowa.R
import com.example.aplikacja_sieciowa.data.model.Channel
import com.example.aplikacja_sieciowa.data.model.ConversationType
import com.example.aplikacja_sieciowa.data.model.Message
import com.example.aplikacja_sieciowa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onDisconnect: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val rightDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val channels by viewModel.channels.collectAsState()
    val currentConversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.messagesForCurrentConversation.collectAsState()
    val messageText by viewModel.messageText.collectAsState()
    val currentNickname by viewModel.currentNickname.collectAsState()
    val directMessages by viewModel.directMessageConversations.collectAsState()
    val currentChannelUsers by viewModel.currentChannelUsers.collectAsState()


    ModalNavigationDrawer(
        drawerState = leftDrawerState,
        drawerContent = {
            LeftDrawer(
                channels = channels,
                directMessages = directMessages,
                currentConversation = currentConversation,
                currentNickname = currentNickname,
                onChannelClick = { channel ->
                    viewModel.joinChannel(channel)
                    scope.launch { leftDrawerState.close() }
                },
                onDMClick = { username ->
                    viewModel.openDirectMessage(username)
                    scope.launch { leftDrawerState.close() }
                },
                onCreateChannel = { channelName ->
                    viewModel.createAndJoinChannel(channelName)
                    scope.launch { leftDrawerState.close() }
                },
                onRefresh = viewModel::refreshChannels,
                onDisconnect = {
                    viewModel.disconnect(onDisconnect)
                }
            )
        },
        gesturesEnabled = true
    ) {

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            ModalNavigationDrawer(
                drawerState = rightDrawerState,
                drawerContent = {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        RightDrawer(
                            users = currentChannelUsers,
                            currentConversation = currentConversation,
                            currentNickname = currentNickname,
                            onUserClick = { username ->
                                viewModel.openDirectMessage(username)
                                scope.launch { rightDrawerState.close() }
                            },
                            onRefresh = viewModel::refreshUsers
                        )
                    }
                },
                gesturesEnabled = currentConversation is ConversationType.ChannelConversation,
                modifier = Modifier.fillMaxSize()
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(
                        topBar = {
                            ChatTopBar(
                                currentConversation = currentConversation,
                                onMenuClick = {
                                    scope.launch { leftDrawerState.open() }
                                },
                                onUsersClick = {
                                    scope.launch { rightDrawerState.open() }
                                }
                            )
                        },
                        containerColor = DiscordDarkBackground
                    ) { padding ->
                        if (currentConversation == null) {
                            NoConversationSelected(
                                modifier = Modifier.padding(padding),
                                onOpenMenu = {
                                    scope.launch { leftDrawerState.open() }
                                }
                            )
                        } else {
                            ChatContent(
                                messages = messages,
                                messageText = messageText,
                                onMessageTextChange = viewModel::updateMessageText,
                                onSendMessage = viewModel::sendMessage,
                                currentNickname = currentNickname,
                                modifier = Modifier.padding(padding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    currentConversation: ConversationType?,
    onMenuClick: () -> Unit,
    onUsersClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (currentConversation) {
                    is ConversationType.ChannelConversation -> {
                        Icon(
                            painter = painterResource(id = R.drawable.hashtag),
                            contentDescription = null,
                            tint = DiscordTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentConversation.channelName.removePrefix("#"),
                            color = DiscordTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    is ConversationType.DirectMessage -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(DiscordBlurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentConversation.username.firstOrNull()?.uppercase() ?: "?",
                                color = DiscordTextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentConversation.username,
                            color = DiscordTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    null -> {
                        Text(
                            text = "Wybierz konwersację",
                            color = DiscordTextMuted
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = DiscordTextPrimary
                )
            }
        },
        actions = {
            if (currentConversation is ConversationType.ChannelConversation) {
                IconButton(onClick = onUsersClick) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Użytkownicy",
                        tint = DiscordTextPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DiscordDarkerBackground
        )
    )
}

@Composable
fun NoConversationSelected(
    modifier: Modifier = Modifier,
    onOpenMenu: () -> Unit
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
                painter = painterResource(id = R.drawable.question_mark),
                contentDescription = null,
                tint = DiscordTextMuted,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Nie wybrano konwersacji",
                style = MaterialTheme.typography.headlineSmall,
                color = DiscordTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Wybierz kanał lub rozpocznij DM",
                style = MaterialTheme.typography.bodyMedium,
                color = DiscordTextSecondary
            )
            Button(
                onClick = onOpenMenu,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiscordBlurple
                )
            ) {
                Text("Otwórz menu")
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
    currentNickname: String?,
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    isOwnMessage = message.sender == currentNickname
                )
            }
        }

        MessageInput(
            text = messageText,
            onTextChange = onMessageTextChange,
            onSend = onSendMessage
        )
    }
}

@Composable
fun MessageItem(
    message: Message,
    isOwnMessage: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    when {
                        message.sender == "SYSTEM" -> DiscordYellow
                        isOwnMessage -> DiscordGreen
                        else -> DiscordBlurple
                    }
                ),
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
                    color = when {
                        message.sender == "SYSTEM" -> DiscordYellow
                        isOwnMessage -> DiscordGreen
                        else -> DiscordTextPrimary
                    },
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
                if (message.isPrivate) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DM",
                        color = DiscordBlurple,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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

@Composable
fun LeftDrawer(
    channels: List<Channel>,
    directMessages: List<String>,
    currentConversation: ConversationType?,
    currentNickname: String?,
    onChannelClick: (String) -> Unit,
    onDMClick: (String) -> Unit,
    onCreateChannel: (String) -> Unit,
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
                    colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
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
        Column(modifier = Modifier.fillMaxSize()) {
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

            LazyColumn(modifier = Modifier.weight(1f)) {
                // CHANNELS SECTION
                item {
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
                }

                items(channels) { channel ->
                    ConversationItem(
                        icon = { painterResource(id = R.drawable.hashtag) },
                        name = channel.name.removePrefix("#"),
                        isSelected = currentConversation is ConversationType.ChannelConversation &&
                                currentConversation.channelName == channel.name,
                        onClick = { onChannelClick(channel.name) }
                    )
                }


                if (directMessages.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "WIADOMOŚCI PRYWATNE",
                            style = MaterialTheme.typography.labelSmall,
                            color = DiscordTextMuted,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp, 12.dp)
                        )
                    }

                    items(directMessages) { username ->
                        DMItem(
                            username = username,
                            isSelected = currentConversation is ConversationType.DirectMessage &&
                                    currentConversation.username == username,
                            onClick = { onDMClick(username) }
                        )
                    }
                }
            }

            Divider(color = DiscordDarkestBackground, thickness = 1.dp)

            TextButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = DiscordRed)
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
fun ConversationItem(
    icon: @Composable () -> androidx.compose.ui.graphics.painter.Painter,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) DiscordChannelSelected else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon(),
            contentDescription = null,
            tint = if (isSelected) DiscordTextPrimary else DiscordTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = if (isSelected) DiscordTextPrimary else DiscordTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun DMItem(
    username: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) DiscordChannelSelected else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(DiscordBlurple),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = DiscordTextPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = username,
            color = if (isSelected) DiscordTextPrimary else DiscordTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun RightDrawer(
    users: List<String>,
    currentConversation: ConversationType?,
    currentNickname: String?,
    onUserClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = DiscordDarkerBackground,
        drawerShape = RoundedCornerShape(
            topStart = 16.dp,
            bottomStart = 16.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp
        ),
        modifier = Modifier.width(240.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DiscordDarkestBackground)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CZŁONKOWIE",
                    style = MaterialTheme.typography.labelSmall,
                    color = DiscordTextMuted,
                    fontWeight = FontWeight.Bold
                )
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

            Divider(color = DiscordDarkestBackground, thickness = 1.dp)

            if (currentConversation is ConversationType.ChannelConversation) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(users) { username ->
                        UserItem(
                            username = username,
                            isCurrentUser = username == currentNickname,
                            onClick = {
                                if (username != currentNickname) {
                                    onUserClick(username)
                                }
                            }
                        )
                    }


                    if (users.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Brak użytkowników",
                                    color = DiscordTextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tylko dla kanałów",
                        color = DiscordTextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun UserItem(
    username: String,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isCurrentUser,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isCurrentUser) DiscordBlurple else DiscordGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                color = DiscordTextPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = username,
                    color = if (isCurrentUser) DiscordBlurple else DiscordTextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                )
                if (isCurrentUser) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(JA)",
                        color = DiscordBlurple,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}