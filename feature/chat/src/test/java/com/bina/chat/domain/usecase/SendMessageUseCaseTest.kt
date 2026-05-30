package com.bina.chat.domain.usecase

import com.bina.chat.domain.model.AgentMessageResult
import com.bina.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendMessageUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = SendMessageUseCase(repository)

    @Test
    fun `GIVEN a user message WHEN invoked THEN delegates to repository and returns result`() = runTest {
        val userMessage = "Quem é o Rick Sanchez?"
        val expected = AgentMessageResult(text = "Rick é um gênio, Morty.")
        coEvery { repository.sendAgentMessage(userMessage) } returns expected

        val result = useCase(userMessage)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.sendAgentMessage(userMessage) }
    }

    @Test
    fun `GIVEN a message that triggers navigation WHEN invoked THEN returns result with navigationEvent`() = runTest {
        val userMessage = "mostre o Morty"
        val expected = AgentMessageResult(
            text = "Aqui está, Morty. *burp*",
            navigationEvent = com.bina.chat.domain.model.ChatNavigationEvent.OpenCharacter(characterId = 2)
        )
        coEvery { repository.sendAgentMessage(userMessage) } returns expected

        val result = useCase(userMessage)

        assertEquals(expected, result)
    }
}
