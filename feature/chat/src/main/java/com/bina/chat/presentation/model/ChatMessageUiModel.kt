package com.bina.chat.presentation.model

import com.bina.chat.domain.model.MessageRole

data class ChatMessageUiModel(
    val role: MessageRole,
    val text: String,
    val isStreaming: Boolean = false
)
