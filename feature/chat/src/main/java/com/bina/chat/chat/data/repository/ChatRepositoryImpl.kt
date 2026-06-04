package com.bina.chat.chat.data.repository

import com.bina.chat.chat.data.datasource.ChatDataSource
import com.bina.chat.chat.domain.model.AgentMessageResult
import com.bina.chat.chat.domain.model.ChatNavigationEvent
import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository
import com.bina.chat.search.data.datasource.CharacterSearchDataSource
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(
    private val dataSource: ChatDataSource,
    private val characterSearch: CharacterSearchDataSource,
    private val persona: String
) : ChatRepository {

    override suspend fun checkAvailability(): ModelAvailability = dataSource.checkAvailability()

    override suspend fun warmup() = dataSource.warmup()

    override fun streamResponse(userMessage: String): Flow<String> =
        dataSource.sendMessageStream(persona + userMessage)

    override suspend fun sendAgentMessage(userMessage: String): AgentMessageResult {
        val toolResponse = dataSource.sendMessageWithTools(persona + userMessage)

        val navigationEvent = toolResponse.functionCalls.firstOrNull()?.let { call ->
            when (call.name) {
                "show_character" -> {
                    val name = call.args["name"] ?: return@let null
                    val id = characterSearch.searchByName(name)
                    if (id != null) ChatNavigationEvent.OpenCharacter(id) else null
                }
                "search_characters" -> {
                    val query = call.args["query"] ?: return@let null
                    ChatNavigationEvent.SearchCharacters(query)
                }
                else -> null
            }
        }

        val text = toolResponse.text?.takeIf { it.isNotBlank() }
            ?: if (navigationEvent != null) {
                "Aqui está, Morty. *burp*"
            } else {
                "Não consegui processar isso."
            }

        return AgentMessageResult(text = text, navigationEvent = navigationEvent)
    }

    override fun close() = dataSource.close()
}
