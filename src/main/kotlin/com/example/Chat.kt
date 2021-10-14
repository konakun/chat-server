package com.example

data class Chat(
    val type: String,
    val message: String,
    val to: String? = null
)
