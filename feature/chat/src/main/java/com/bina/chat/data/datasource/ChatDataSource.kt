package com.bina.chat.data.datasource

import com.bina.chat.domain.model.ModelAvailability
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    suspend fun checkAvailability(): ModelAvailability
    suspend fun warmup()
    fun sendMessageStream(prompt: String): Flow<String>
    fun close()
}
