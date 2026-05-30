package com.bina.chat.chat.domain.usecase

import com.bina.chat.chat.domain.model.AgentMessageResult
import com.bina.chat.chat.domain.repository.ChatRepository
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
        val expected = AgentMessageResult(text = "Rick é um gênio.")
        coEvery { repository.sendAgentMessage(userMessage) } returns expected

        val result = useCase(userMessage)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.sendAgentMessage(userMessage) }
    }
}
