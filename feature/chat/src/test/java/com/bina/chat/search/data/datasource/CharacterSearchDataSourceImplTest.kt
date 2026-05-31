package com.bina.chat.search.data.datasource

import com.bina.chat.search.data.model.CharacterSearchResponse
import com.bina.chat.search.data.model.CharacterSearchResult
import com.bina.chat.search.data.remote.CharacterSearchApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterSearchDataSourceImplTest {

    private val api: CharacterSearchApiService = mockk()
    private val dataSource = CharacterSearchDataSourceImpl(api)

    @Test
    fun `GIVEN api returns results WHEN searchByName THEN returns first result id`() = runTest {
        coEvery { api.searchByName("Rick") } returns CharacterSearchResponse(
            results = listOf(
                CharacterSearchResult(id = 1, name = "Rick Sanchez"),
                CharacterSearchResult(id = 2, name = "Rick J-22")
            )
        )

        val result = dataSource.searchByName("Rick")

        assertEquals(1, result)
    }

    @Test
    fun `GIVEN api returns empty results WHEN searchByName THEN returns null`() = runTest {
        coEvery { api.searchByName("Inexistente") } returns CharacterSearchResponse(results = emptyList())

        val result = dataSource.searchByName("Inexistente")

        assertNull(result)
    }

    @Test
    fun `GIVEN api returns null results WHEN searchByName THEN returns null`() = runTest {
        coEvery { api.searchByName("X") } returns CharacterSearchResponse(results = null)

        val result = dataSource.searchByName("X")

        assertNull(result)
    }

    @Test
    fun `GIVEN api throws exception WHEN searchByName THEN returns null`() = runTest {
        coEvery { api.searchByName(any()) } throws RuntimeException("Network error")

        val result = dataSource.searchByName("Rick")

        assertNull(result)
    }
}