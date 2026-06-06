package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.EpisodeDataSource
import com.bina.character_details.data.model.EpisodeData
import com.bina.network.NetworkResult
import com.bina.network.data
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EpisodeRepositoryImplTest {

    private val dataSource: EpisodeDataSource = mockk()
    private val repository = EpisodeRepositoryImpl(dataSource)

    private fun episodeData(id: Int = 1, name: String = "Pilot") =
        EpisodeData(id = id, name = name, episode = "S01E01", airDate = "December 2, 2013")

    @Test
    fun `GIVEN dataSource returns episodes WHEN getEpisodes THEN returns Success with mapped domains`() = runTest {
        val ids = listOf(1, 2)
        coEvery { dataSource.getEpisodes(ids) } returns listOf(
            episodeData(1, "Pilot"),
            episodeData(2, "Lawnmower Dog")
        )

        val result = repository.getEpisodes(ids)

        assertTrue(result is NetworkResult.Success)
        val episodes = (result as NetworkResult.Success).data
        assertEquals(2, episodes.size)
        assertEquals("Pilot", episodes[0].name)
        assertEquals("Lawnmower Dog", episodes[1].name)
        coVerify(exactly = 1) { dataSource.getEpisodes(ids) }
    }

    @Test
    fun `GIVEN dataSource returns empty list WHEN getEpisodes THEN returns Success with empty list`() = runTest {
        coEvery { dataSource.getEpisodes(emptyList()) } returns emptyList()

        val result = repository.getEpisodes(emptyList())

        assertTrue(result is NetworkResult.Success)
        assertEquals(emptyList<Any>(), (result as NetworkResult.Success).data)
    }

    @Test
    fun `GIVEN dataSource throws WHEN getEpisodes THEN returns NetworkError`() = runTest {
        coEvery { dataSource.getEpisodes(any()) } throws RuntimeException("Network error")

        val result = repository.getEpisodes(listOf(1))

        assertTrue(result is NetworkResult.NetworkError)
        assertEquals("Network error", (result as NetworkResult.NetworkError).exception.message)
    }
}