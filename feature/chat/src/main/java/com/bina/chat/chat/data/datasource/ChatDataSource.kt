package com.bina.chat.chat.data.datasource

import com.bina.chat.chat.data.model.ToolResponse
import com.bina.chat.chat.domain.model.ModelAvailability
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    suspend fun checkAvailability(): ModelAvailability
    suspend fun warmup()
    fun sendMessageStream(prompt: String): Flow<String>
    suspend fun sendMessageWithTools(prompt: String): ToolResponse
    fun close()
}
