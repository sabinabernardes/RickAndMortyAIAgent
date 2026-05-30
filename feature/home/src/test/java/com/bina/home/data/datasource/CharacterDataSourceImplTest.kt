package com.bina.home.data.datasource

import com.bina.home.data.model.CharacterData
import com.bina.home.data.model.CharacterResponse
import com.bina.home.data.model.LocationData
import com.bina.home.data.remote.RickAndMortyApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class CharacterDataSourceImplTest {
    private lateinit var apiService: RickAndMortyApiService
    private lateinit var dataSource: CharacterDataSourceImpl

    @Before
    fun setUp() {
        apiService = mockk()
        dataSource = CharacterDataSourceImpl(apiService)
    }

    @Test
    fun `given api returns data when getCharacters then returns character list`() = runBlocking {
        // Given
        val expectedList = listOf(CharacterData(id = 1, name = "Rick", status = "Alive", species = "Human", image = "img", location = LocationData("Earth", "")))
        val response = CharacterResponse(results = expectedList)
        coEvery { apiService.getCharacters("", 1) } returns response

        // When
        val result = dataSource.getCharacters("", 1)

        // Then
        assertEquals(expectedList, result)
        coVerify { apiService.getCharacters("", 1) }
    }

    @Test
    fun `given api throws exception when getCharacters then throws exception`() = runBlocking {
        // Given
        coEvery { apiService.getCharacters("", 1) } throws RuntimeException("API error")

        // When & Then
        try {
            dataSource.getCharacters("", 1)
            assertFalse("Exception esperada não foi lançada", true)
        } catch (e: Exception) {
            assertEquals("API error", e.message)
        }
        coVerify { apiService.getCharacters("", 1) }
    }
}
