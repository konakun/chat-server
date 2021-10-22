package com.example.chat

data class Chat(
    val type: String,
    val message: String,
    val to: String? = null
)
