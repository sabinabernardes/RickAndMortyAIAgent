package com.bina.chat.chat.data.repository

import com.bina.chat.chat.data.datasource.ChatDataSource
import com.bina.chat.chat.data.model.FunctionCallData
import com.bina.chat.chat.data.model.ToolResponse
import com.bina.chat.chat.domain.model.ChatNavigationEvent
import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.search.data.datasource.CharacterSearchDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRepositoryImplTest {

    private val dataSource: ChatDataSource = mockk()
    private val characterSearch: CharacterSearchDataSource = mockk()
    private val repository = ChatRepositoryImpl(dataSource, characterSearch)

    @Test
    fun `GIVEN dataSource returns Available WHEN checkAvailability THEN returns Available`() = runTest {
        coEvery { dataSource.checkAvailability() } returns ModelAvailability.Available

        val result = repository.checkAvailability()

        assertEquals(ModelAvailability.Available, result)
        coVerify(exactly = 1) { dataSource.checkAvailability() }
    }

    @Test
    fun `GIVEN user message WHEN streamResponse THEN RICK_PERSONA is prepended to prompt`() = runTest {
        coEvery { dataSource.sendMessageStream(any()) } returns flowOf("Olá, Morty.")

        val results = repository.streamResponse("Quem é você?").toList()

        assertEquals(listOf("Olá, Morty."), results)
        coVerify {
            dataSource.sendMessageStream(match { it.endsWith("Quem é você?") && it.length > "Quem é você?".length })
        }
    }

    @Test
    fun `GIVEN show_character call with found character WHEN sendAgentMessage THEN returns OpenCharacter event`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "Aqui está!",
            functionCalls = listOf(FunctionCallData(name = "show_character", args = mapOf("name" to "Rick")))
        )
        coEvery { characterSearch.searchByName("Rick") } returns 1

        val result = repository.sendAgentMessage("Mostre o Rick")

        assertTrue(result.navigationEvent is ChatNavigationEvent.OpenCharacter)
        assertEquals(1, (result.navigationEvent as ChatNavigationEvent.OpenCharacter).characterId)
    }

    @Test
    fun `GIVEN show_character call with unfound character WHEN sendAgentMessage THEN no navigation event`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "Não encontrei.",
            functionCalls = listOf(FunctionCallData(name = "show_character", args = mapOf("name" to "Unknown")))
        )
        coEvery { characterSearch.searchByName("Unknown") } returns null

        val result = repository.sendAgentMessage("Mostre o Unknown")

        assertNull(result.navigationEvent)
        assertEquals("Não encontrei.", result.text)
    }

    @Test
    fun `GIVEN show_character call without name arg WHEN sendAgentMessage THEN no navigation event`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "Sem nome.",
            functionCalls = listOf(FunctionCallData(name = "show_character", args = emptyMap()))
        )

        val result = repository.sendAgentMessage("Mostre")

        assertNull(result.navigationEvent)
        assertEquals("Sem nome.", result.text)
    }

    @Test
    fun `GIVEN search_characters call WHEN sendAgentMessage THEN returns SearchCharacters event and fallback text`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = null,
            functionCalls = listOf(FunctionCallData(name = "search_characters", args = mapOf("query" to "Morty")))
        )

        val result = repository.sendAgentMessage("Busque Morty")

        assertTrue(result.navigationEvent is ChatNavigationEvent.SearchCharacters)
        assertEquals("Morty", (result.navigationEvent as ChatNavigationEvent.SearchCharacters).query)
        assertEquals("Aqui está, Morty. *burp*", result.text)
    }

    @Test
    fun `GIVEN unknown function call WHEN sendAgentMessage THEN no navigation event and text from response`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "Resposta do Rick.",
            functionCalls = listOf(FunctionCallData(name = "unknown_tool", args = emptyMap()))
        )

        val result = repository.sendAgentMessage("Algo")

        assertNull(result.navigationEvent)
        assertEquals("Resposta do Rick.", result.text)
    }

    @Test
    fun `GIVEN no function calls and text present WHEN sendAgentMessage THEN returns text without navigation`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "Rick responde.",
            functionCalls = emptyList()
        )

        val result = repository.sendAgentMessage("Pergunta")

        assertEquals("Rick responde.", result.text)
        assertNull(result.navigationEvent)
    }

    @Test
    fun `GIVEN null text and no function calls WHEN sendAgentMessage THEN returns fallback error text`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = null,
            functionCalls = emptyList()
        )

        val result = repository.sendAgentMessage("Pergunta")

        assertEquals("Não consegui processar isso.", result.text)
        assertNull(result.navigationEvent)
    }

    @Test
    fun `GIVEN blank text with navigation event WHEN sendAgentMessage THEN fallback text is used`() = runTest {
        coEvery { dataSource.sendMessageWithTools(any()) } returns ToolResponse(
            text = "   ",
            functionCalls = listOf(FunctionCallData(name = "search_characters", args = mapOf("query" to "Rick")))
        )

        val result = repository.sendAgentMessage("Busque Rick")

        assertEquals("Aqui está, Morty. *burp*", result.text)
    }
}