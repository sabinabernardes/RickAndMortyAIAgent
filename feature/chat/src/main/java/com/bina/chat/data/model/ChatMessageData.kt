package com.bina.chat.data.model

data class ChatMessageData(
    val role: String,
    val text: String,
    val timestampMs: Long
)
