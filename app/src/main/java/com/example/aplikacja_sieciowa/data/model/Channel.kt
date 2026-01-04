package com.example.aplikacja_sieciowa.data.model

data class Channel(
    val name: String,
    val memberCount: Int = 0,
    val members: List<String> = emptyList()
)
