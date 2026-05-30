package com.bina.chat.chat.domain.usecase

import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository

class CheckModelAvailabilityUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): ModelAvailability = repository.checkAvailability()
}
