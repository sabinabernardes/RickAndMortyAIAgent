package com.bina.chat.chat.presentation.model

import com.bina.chat.chat.domain.model.MessageRole

data class ChatMessageUiModel(
    val role: MessageRole,
    val text: String,
    val isStreaming: Boolean = false
)
