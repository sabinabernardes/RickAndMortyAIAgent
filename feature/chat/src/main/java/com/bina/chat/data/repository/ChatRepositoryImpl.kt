package com.bina.chat.data.repository

import com.bina.chat.data.datasource.ChatDataSource
import com.bina.chat.domain.model.ModelAvailability
import com.bina.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

private const val RICK_PERSONA = """Você é um especialista apaixonado no universo de Rick and Morty.
Responda com o tom sarcástico, brilhante e impaciente do Rick Sanchez.
Use gírias do Rick ocasionalmente (como "Morty", "wubba lubba dub dub", "burp").
Seja direto e um pouco condescendente, mas sempre preciso sobre o universo do show.
Responda a seguinte pergunta como Rick responderia:  mais com poucas palavras """

class ChatRepositoryImpl(private val dataSource: ChatDataSource) : ChatRepository {

    override suspend fun checkAvailability(): ModelAvailability = dataSource.checkAvailability()

    override suspend fun warmup() = dataSource.warmup()

    override fun streamResponse(userMessage: String): Flow<String> {
        val fullPrompt = RICK_PERSONA + userMessage
        return dataSource.sendMessageStream(fullPrompt)
    }

    override fun close() = dataSource.close()
}
