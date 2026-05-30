package com.bina.home.domain.usecase

import androidx.paging.PagingData
import com.bina.home.domain.model.CharacterDomain

import com.bina.home.domain.repository.HomeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetCharactersUseCaseTest {
    private lateinit var repository: HomeRepository
    private lateinit var useCase: GetCharactersUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetCharactersUseCase(repository)
    }

    @Test
    fun `given repository returns PagingData when invoke then emits PagingData`() = runTest {
        // Given
        val pagingData = PagingData.from(listOf(CharacterDomain(1, "Rick", "Alive", "Human", "img", "Earth")))
        coEvery { repository.getCharacters("") } returns flowOf(pagingData)

        // When
        val result = useCase.invoke("").first()

        // Then
        assertEquals(pagingData, result)
    }
}

