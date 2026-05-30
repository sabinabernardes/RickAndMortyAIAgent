package com.bina.chat.chat.domain.model

data class ChatMessageDomain(
    val role: MessageRole,
    val text: String,
    val timestampMs: Long
)
