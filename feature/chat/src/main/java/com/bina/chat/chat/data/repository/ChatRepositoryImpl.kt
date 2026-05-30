package com.bina.chat.chat.data.repository

import com.bina.chat.chat.data.datasource.ChatDataSource
import com.bina.chat.chat.domain.model.AgentMessageResult
import com.bina.chat.chat.domain.model.ChatNavigationEvent
import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository
import com.bina.chat.search.data.datasource.CharacterSearchDataSource
import kotlinx.coroutines.flow.Flow

private const val RICK_PERSONA = """Você é um especialista apaixonado no universo de Rick and Morty.
Responda com o tom sarcástico, brilhante e impaciente do Rick Sanchez.
Use gírias do Rick ocasionalmente (como "Morty", "wubba lubba dub dub", "burp").
Seja direto e um pouco condescendente, mas sempre preciso sobre o universo do show.
Quando o usuário pedir para VER ou MOSTRAR um personagem específico, use a função show_character.
Quando o usuário pedir para BUSCAR ou LISTAR personagens por nome, use a função search_characters.
Responda a seguinte pergunta como Rick responderia: """

class ChatRepositoryImpl(
    private val dataSource: ChatDataSource,
    private val characterSearch: CharacterSearchDataSource
) : ChatRepository {

    override suspend fun checkAvailability(): ModelAvailability = dataSource.checkAvailability()

    override suspend fun warmup() = dataSource.warmup()

    override fun streamResponse(userMessage: String): Flow<String> =
        dataSource.sendMessageStream(RICK_PERSONA + userMessage)

    override suspend fun sendAgentMessage(userMessage: String): AgentMessageResult {
        val toolResponse = dataSource.sendMessageWithTools(RICK_PERSONA + userMessage)

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
            ?: if (navigationEvent != null) "Aqui está, Morty. *burp*"
               else "Não consegui processar isso."

        return AgentMessageResult(text = text, navigationEvent = navigationEvent)
    }

    override fun close() = dataSource.close()
}
