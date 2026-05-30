package com.bina.chat.domain.usecase

import com.bina.chat.domain.repository.ChatRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendMessageUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = SendMessageUseCase(repository)

    @Test
    fun `GIVEN a user message WHEN invoked THEN delegates to repository and returns stream`() = runTest {
        val userMessage = "Quem é o Rick Sanchez?"
        val expectedChunks = listOf("Rick ", "é ", "um gênio.")
        every { repository.streamResponse(userMessage) } returns flowOf(*expectedChunks.toTypedArray())

        val result = useCase(userMessage).toList()

        assertEquals(expectedChunks, result)
        verify(exactly = 1) { repository.streamResponse(userMessage) }
    }
}
