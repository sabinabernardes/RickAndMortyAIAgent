package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.CharacterDetailsDataSource
import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.data.model.LocationDetailsData
import com.bina.character_details.data.model.OriginData
import com.bina.network.NetworkResult
import com.bina.network.data
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

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
    fun `GIVEN dataSource returns data WHEN getCharacterDetails THEN returns Success with mapped domain`() = runTest {
        coEvery { dataSource.getCharacterDetails(1) } returns Response.success(characterData(id = 1, name = "Rick Sanchez"))

        val result = repository.getCharacterDetails(1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.id)
        assertEquals("Rick Sanchez", result.data.name)
        coVerify(exactly = 1) { dataSource.getCharacterDetails(1) }
    }

    @Test
    fun `GIVEN dataSource returns 404 WHEN getCharacterDetails THEN returns BusinessError`() = runTest {
        coEvery { dataSource.getCharacterDetails(any()) } returns Response.error(
            404, "not found".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.getCharacterDetails(99)

        assertTrue(result is NetworkResult.BusinessError)
        assertEquals(404, (result as NetworkResult.BusinessError).code)
    }

    @Test
    fun `GIVEN dataSource throws WHEN getCharacterDetails THEN returns NetworkError`() = runTest {
        coEvery { dataSource.getCharacterDetails(any()) } throws RuntimeException("Not found")

        val result = repository.getCharacterDetails(99)

        assertTrue(result is NetworkResult.NetworkError)
        assertEquals("Not found", (result as NetworkResult.NetworkError).exception.message)
    }
}