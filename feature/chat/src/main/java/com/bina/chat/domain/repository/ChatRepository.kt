package com.bina.chat.domain.repository

import com.bina.chat.domain.model.ModelAvailability
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun checkAvailability(): ModelAvailability
    suspend fun warmup()
    fun streamResponse(userMessage: String): Flow<String>
    fun close()
}
