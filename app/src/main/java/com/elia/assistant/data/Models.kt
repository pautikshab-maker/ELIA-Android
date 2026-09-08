package com.elia.assistant.data

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val role: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
