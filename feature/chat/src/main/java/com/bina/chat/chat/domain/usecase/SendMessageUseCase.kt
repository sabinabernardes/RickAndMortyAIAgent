package com.bina.chat.chat.domain.usecase

import com.bina.chat.chat.domain.model.AgentMessageResult
import com.bina.chat.chat.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(userMessage: String): AgentMessageResult =
        repository.sendAgentMessage(userMessage)
}
