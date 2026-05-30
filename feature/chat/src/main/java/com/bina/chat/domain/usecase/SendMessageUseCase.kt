package com.bina.chat.domain.usecase

import com.bina.chat.domain.model.AgentMessageResult
import com.bina.chat.domain.repository.ChatRepository

class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(userMessage: String): AgentMessageResult =
        repository.sendAgentMessage(userMessage)
}
