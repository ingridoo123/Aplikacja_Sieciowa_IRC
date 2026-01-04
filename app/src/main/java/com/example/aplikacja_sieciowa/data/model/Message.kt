package com.example.aplikacja_sieciowa.data.model

data class Message(
    val channel: String,
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPrivate: Boolean = false
)
