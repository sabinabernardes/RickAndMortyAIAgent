package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.CharacterDetailsDataSource
import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.data.model.LocationDetailsData
import com.bina.character_details.data.model.OriginData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterDetailsRepositoryImplTest {

    private val dataSource: CharacterDetailsDataSource = mockk()
    private val repository = CharacterDetailsRepositoryImpl(dataSource)

    private fun characterData(id: Int = 1, name: String = "Rick") = CharacterDetailsData(
        id = id, name = name, status = "Alive", species = "Human",
        type = "", gender = "Male",
        origin = OriginData("Earth", ""),
        location = LocationDetailsData("Earth", ""),
        image = "img", episode = emptyList(), url = "", created = ""
    )

    @Test
    fun `GIVEN dataSource returns data WHEN getCharacterDetails THEN maps and returns domain`() = runTest {
        coEvery { dataSource.getCharacterDetails(1) } returns characterData(id = 1, name = "Rick Sanchez")

        val result = repository.getCharacterDetails(1)

        assertEquals(1, result.id)
        assertEquals("Rick Sanchez", result.name)
        coVerify(exactly = 1) { dataSource.getCharacterDetails(1) }
    }

    @Test
    fun `GIVEN dataSource throws WHEN getCharacterDetails THEN exception propagates`() = runTest {
        coEvery { dataSource.getCharacterDetails(any()) } throws RuntimeException("Not found")

        val exception = runCatching { repository.getCharacterDetails(99) }.exceptionOrNull()

        assertEquals("Not found", exception?.message)
    }
}
