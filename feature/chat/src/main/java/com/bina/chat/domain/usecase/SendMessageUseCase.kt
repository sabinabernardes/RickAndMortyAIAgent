package com.bina.chat.domain.usecase

import com.bina.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class SendMessageUseCase(private val repository: ChatRepository) {
    operator fun invoke(userMessage: String): Flow<String> = repository.streamResponse(userMessage)
}
