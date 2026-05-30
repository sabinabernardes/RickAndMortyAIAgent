package com.bina.chat.chat.data.datasource

import com.bina.chat.chat.data.model.FunctionCallData
import com.bina.chat.chat.data.model.ToolResponse
import com.bina.chat.chat.domain.model.ModelAvailability
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class ChatDataSourceImpl(private val model: GenerativeModel) : ChatDataSource {

    override suspend fun checkAvailability(): ModelAvailability = ModelAvailability.Available

    override suspend fun warmup() = Unit

    override fun sendMessageStream(prompt: String): Flow<String> =
        model.generateContentStream(prompt).mapNotNull { runCatching { it.text }.getOrNull() }

    override suspend fun sendMessageWithTools(prompt: String): ToolResponse {
        val response = model.generateContent(prompt)
        return ToolResponse(
            text = response.text,
            functionCalls = response.functionCalls.map { call ->
                FunctionCallData(name = call.name, args = call.args)
            }
        )
    }

    override fun close() = Unit
}
