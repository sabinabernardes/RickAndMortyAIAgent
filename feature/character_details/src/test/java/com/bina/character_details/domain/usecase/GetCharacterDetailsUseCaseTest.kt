package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCharacterDetailsUseCaseTest {

    private val repository: CharacterDetailsRepository = mockk()
    private val useCase = GetCharacterDetailsUseCase(repository)

    @Test
    fun `GIVEN a character id WHEN invoke is called THEN should return character details`() = runBlocking {
        // GIVEN
        val id = 1
        val expectedCharacter = CharacterDetailsDomain(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            gender = "Male",
            origin = "Earth",
            location = "Earth",
            image = "url",
            episodeUrls = emptyList()
        )
        coEvery { repository.getCharacterDetails(id) } returns expectedCharacter

        // WHEN
        val result = useCase(id)

        // THEN
        assertEquals(expectedCharacter, result)
    }
}
