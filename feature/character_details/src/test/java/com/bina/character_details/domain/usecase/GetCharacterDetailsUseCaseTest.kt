package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import com.bina.network.NetworkResult
import com.bina.network.data
import com.bina.network.successOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCharacterDetailsUseCaseTest {

    private val repository: CharacterDetailsRepository = mockk()
    private val useCase = GetCharacterDetailsUseCase(repository)

    @Test
    fun `GIVEN a character id WHEN invoke is called THEN should return Success with character details`() = runTest {
        val id = 1
        val expectedCharacter = CharacterDetailsDomain(
            id = 1, name = "Rick Sanchez", status = "Alive", species = "Human",
            gender = "Male", origin = "Earth", location = "Earth",
            image = "url", episodeUrls = emptyList()
        )
        coEvery { repository.getCharacterDetails(id) } returns successOf(expectedCharacter)

        val result = useCase(id)

        assertTrue(result is NetworkResult.Success)
        assertEquals(expectedCharacter, (result as NetworkResult.Success).data)
    }
}