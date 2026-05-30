package com.bina.chat.chat.domain.usecase

import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckModelAvailabilityUseCaseTest {

    private val repository: ChatRepository = mockk()
    private val useCase = CheckModelAvailabilityUseCase(repository)

    @Test
    fun `GIVEN repository returns Available WHEN invoked THEN returns Available`() = runTest {
        coEvery { repository.checkAvailability() } returns ModelAvailability.Available

        val result = useCase()

        assertEquals(ModelAvailability.Available, result)
        coVerify(exactly = 1) { repository.checkAvailability() }
    }

    @Test
    fun `GIVEN repository returns Unavailable WHEN invoked THEN returns Unavailable`() = runTest {
        coEvery { repository.checkAvailability() } returns ModelAvailability.Unavailable

        val result = useCase()

        assertEquals(ModelAvailability.Unavailable, result)
    }

    @Test
    fun `GIVEN repository returns Downloadable WHEN invoked THEN returns Downloadable`() = runTest {
        coEvery { repository.checkAvailability() } returns ModelAvailability.Downloadable

        val result = useCase()

        assertEquals(ModelAvailability.Downloadable, result)
    }
}
