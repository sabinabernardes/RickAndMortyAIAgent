package com.bina.chat.data.datasource

import com.bina.chat.domain.model.ModelAvailability
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class ChatDataSourceImpl(private val model: GenerativeModel) : ChatDataSource {

    override suspend fun checkAvailability(): ModelAvailability = ModelAvailability.Available

    override suspend fun warmup() = Unit

    override fun sendMessageStream(prompt: String): Flow<String> =
        model.generateContentStream(prompt).mapNotNull { runCatching { it.text }.getOrNull() }

    override fun close() = Unit
}