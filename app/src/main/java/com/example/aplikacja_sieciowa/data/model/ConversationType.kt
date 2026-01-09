package com.example.aplikacja_sieciowa.data.model

sealed class ConversationType {
    data class ChannelConversation(val channelName: String) : ConversationType()
    data class DirectMessage(val username: String) : ConversationType()
}