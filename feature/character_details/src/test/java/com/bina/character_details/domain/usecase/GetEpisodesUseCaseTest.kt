package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.repository.EpisodeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetEpisodesUseCaseTest {

    private val repository: EpisodeRepository = mockk()
    private val useCase = GetEpisodesUseCase(repository)

    @Test
    fun `GIVEN ids WHEN invoke THEN delegates to repository and returns list`() = runTest {
        val ids = listOf(1, 2)
        val expected = listOf(
            EpisodeDomain(1, "Pilot", "S01E01", "December 2, 2013"),
            EpisodeDomain(2, "Lawnmower Dog", "S01E02", "December 9, 2013")
        )
        coEvery { repository.getEpisodes(ids) } returns expected

        val result = useCase(ids)

        assertEquals(expected, result)
        coVerify(exactly = 1) { repository.getEpisodes(ids) }
    }

    @Test
    fun `GIVEN empty list WHEN invoke THEN returns empty list`() = runTest {
        coEvery { repository.getEpisodes(emptyList()) } returns emptyList()

        val result = useCase(emptyList())

        assertEquals(emptyList<EpisodeDomain>(), result)
    }
}