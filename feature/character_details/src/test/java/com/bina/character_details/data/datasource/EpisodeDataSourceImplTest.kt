package com.bina.character_details.data.datasource

import com.bina.character_details.data.model.EpisodeData
import com.bina.character_details.data.remote.EpisodeApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

// ADR-010: a API retorna objeto único (não array) quando há apenas um ID
class EpisodeDataSourceImplTest {

    private val api: EpisodeApiService = mockk()
    private val dataSource = EpisodeDataSourceImpl(api)

    private fun episodeData(id: Int = 1) =
        EpisodeData(id = id, name = "Pilot", episode = "S01E01", airDate = "December 2, 2013")

    @Test
    fun `GIVEN single id WHEN getEpisodes THEN calls getEpisodeSingle`() = runTest {
        coEvery { api.getEpisodeSingle(1) } returns Response.success(episodeData(1))

        val result = dataSource.getEpisodes(listOf(1))

        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
        coVerify(exactly = 1) { api.getEpisodeSingle(1) }
        coVerify(exactly = 0) { api.getEpisodes(any()) }
    }

    @Test
    fun `GIVEN multiple ids WHEN getEpisodes THEN calls getEpisodes with comma-separated ids`() = runTest {
        coEvery { api.getEpisodes("1,2,3") } returns Response.success(
            listOf(episodeData(1), episodeData(2), episodeData(3))
        )

        val result = dataSource.getEpisodes(listOf(1, 2, 3))

        assertEquals(3, result.size)
        coVerify(exactly = 1) { api.getEpisodes("1,2,3") }
        coVerify(exactly = 0) { api.getEpisodeSingle(any()) }
    }

    @Test
    fun `GIVEN two ids WHEN getEpisodes THEN calls getEpisodes not getEpisodeSingle`() = runTest {
        coEvery { api.getEpisodes("5,10") } returns Response.success(
            listOf(episodeData(5), episodeData(10))
        )

        val result = dataSource.getEpisodes(listOf(5, 10))

        assertEquals(2, result.size)
        coVerify(exactly = 0) { api.getEpisodeSingle(any()) }
    }
}