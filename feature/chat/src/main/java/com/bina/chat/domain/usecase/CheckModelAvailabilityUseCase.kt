package com.bina.chat.domain.usecase

import com.bina.chat.domain.model.ModelAvailability
import com.bina.chat.domain.repository.ChatRepository

class CheckModelAvailabilityUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(): ModelAvailability = repository.checkAvailability()
}
