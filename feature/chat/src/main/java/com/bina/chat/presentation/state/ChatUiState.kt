package com.bina.chat.presentation.state

import com.bina.chat.presentation.model.ChatMessageUiModel

sealed class ChatUiState {
    object Initializing : ChatUiState()
    object ModelUnavailable : ChatUiState()
    data class Conversation(
        val messages: List<ChatMessageUiModel>,
        val isAiTyping: Boolean,
        val errorMessage: String? = null
    ) : ChatUiState()
}