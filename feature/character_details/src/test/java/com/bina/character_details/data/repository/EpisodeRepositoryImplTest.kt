package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.EpisodeDataSource
import com.bina.character_details.data.model.EpisodeData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeRepositoryImplTest {

    private val dataSource: EpisodeDataSource = mockk()
    private val repository = EpisodeRepositoryImpl(dataSource)

    private fun episodeData(id: Int = 1, name: String = "Pilot") =
        EpisodeData(id = id, name = name, episode = "S01E01", airDate = "December 2, 2013")

    @Test
    fun `GIVEN dataSource returns episodes WHEN getEpisodes THEN all are mapped to domain`() = runTest {
        val ids = listOf(1, 2)
        coEvery { dataSource.getEpisodes(ids) } returns listOf(
            episodeData(1, "Pilot"),
            episodeData(2, "Lawnmower Dog")
        )

        val result = repository.getEpisodes(ids)

        assertEquals(2, result.size)
        assertEquals("Pilot", result[0].name)
        assertEquals("Lawnmower Dog", result[1].name)
        coVerify(exactly = 1) { dataSource.getEpisodes(ids) }
    }

    @Test
    fun `GIVEN dataSource returns empty list WHEN getEpisodes THEN returns empty list`() = runTest {
        coEvery { dataSource.getEpisodes(emptyList()) } returns emptyList()

        val result = repository.getEpisodes(emptyList())

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun `GIVEN dataSource throws WHEN getEpisodes THEN exception propagates`() = runTest {
        coEvery { dataSource.getEpisodes(any()) } throws RuntimeException("Network error")

        val exception = runCatching { repository.getEpisodes(listOf(1)) }.exceptionOrNull()

        assertEquals("Network error", exception?.message)
    }
}
