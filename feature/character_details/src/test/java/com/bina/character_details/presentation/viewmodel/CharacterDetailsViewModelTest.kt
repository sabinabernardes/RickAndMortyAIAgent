package com.bina.character_details.presentation.viewmodel

import app.cash.turbine.test
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.character_details.analytics.CharacterDetailsEvent
import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.domain.usecase.GetEpisodesUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.mapper.EpisodeUiMapper
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import com.bina.character_details.presentation.state.EpisodesState
import com.bina.logging.AppLogger
import com.bina.network.NetworkResult
import com.bina.network.successOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharacterDetailsViewModelTest {

    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase = mockk()
    private val getEpisodesUseCase: GetEpisodesUseCase = mockk(relaxed = true)
    private val uiMapper = CharacterDetailsUiMapper()
    private val episodeUiMapper = EpisodeUiMapper()
    private val logger = mockk<AppLogger>(relaxed = true)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)
    private val performance = mockk<PerformanceTracker>(relaxed = true)
    private lateinit var viewModel: CharacterDetailsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { performance.stopTrace(any()) } returns 100L
        coEvery { getEpisodesUseCase(any()) } returns successOf(emptyList())
        viewModel = CharacterDetailsViewModel(
            getCharacterDetailsUseCase, getEpisodesUseCase, uiMapper, episodeUiMapper,
            logger, analytics, performance
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a character id WHEN getCharacterDetails is called THEN should emit success state`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = 1, name = "Rick Sanchez", status = "Alive", species = "Human",
            gender = "Male", origin = "Earth", location = "Earth",
            image = "url", episodeUrls = emptyList()
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assert(state is CharacterDetailsUiState.Success)
            assertEquals(characterDomain.name, (state as CharacterDetailsUiState.Success).character.name)
        }
    }

    @Test
    fun `GIVEN a character id WHEN getCharacterDetails fails THEN should emit error state`() = runTest {
        val id = 1
        val errorMessage = "Error loading character"
        coEvery { getCharacterDetailsUseCase(id) } returns NetworkResult.NetworkError(Exception(errorMessage))

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assert(state is CharacterDetailsUiState.Error)
            assertEquals(errorMessage, (state as CharacterDetailsUiState.Error).message)
        }
    }

    @Test
    fun `GIVEN character with episodes WHEN getCharacterDetails THEN episodesState is Success`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = 1, name = "Rick", status = "Alive", species = "Human", gender = "Male",
            origin = "Earth", location = "Earth", image = "url",
            episodeUrls = listOf(
                "https://rickandmortyapi.com/api/episode/1",
                "https://rickandmortyapi.com/api/episode/2"
            )
        )
        val episodes = listOf(
            EpisodeDomain(1, "Pilot", "S01E01", "December 2, 2013"),
            EpisodeDomain(2, "Lawnmower Dog", "S01E02", "December 9, 2013")
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)
        coEvery { getEpisodesUseCase(listOf(1, 2)) } returns successOf(episodes)

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as CharacterDetailsUiState.Success
            assert(state.episodesState is EpisodesState.Success)
            assertEquals(2, (state.episodesState as EpisodesState.Success).episodes.size)
            assertEquals("Pilot", (state.episodesState as EpisodesState.Success).episodes[0].name)
        }
    }

    @Test
    fun `GIVEN episodes use case returns NetworkError WHEN getCharacterDetails THEN episodesState is Error`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = 1, name = "Rick", status = "Alive", species = "Human", gender = "Male",
            origin = "Earth", location = "Earth", image = "url",
            episodeUrls = listOf("https://rickandmortyapi.com/api/episode/1")
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)
        coEvery { getEpisodesUseCase(listOf(1)) } returns NetworkResult.NetworkError(RuntimeException("Episodes error"))

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as CharacterDetailsUiState.Success
            assert(state.episodesState is EpisodesState.Error)
            assertEquals("Episodes error", (state.episodesState as EpisodesState.Error).message)
        }
    }

    @Test
    fun `GIVEN character with no episodes WHEN getCharacterDetails THEN episodesState is Success with empty list`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = 1, name = "Rick", status = "Alive", species = "Human", gender = "Male",
            origin = "Earth", location = "Earth", image = "url",
            episodeUrls = emptyList()
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)
        coEvery { getEpisodesUseCase(emptyList()) } returns successOf(emptyList())

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as CharacterDetailsUiState.Success
            assert(state.episodesState is EpisodesState.Success)
            assertEquals(0, (state.episodesState as EpisodesState.Success).episodes.size)
        }
    }

    @Test
    fun `GIVEN episode url WHEN getCharacterDetails THEN id is parsed from url path`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = 1, name = "Rick", status = "Alive", species = "Human", gender = "Male",
            origin = "Earth", location = "Earth", image = "url",
            episodeUrls = listOf("https://rickandmortyapi.com/api/episode/42")
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)
        coEvery { getEpisodesUseCase(listOf(42)) } returns successOf(
            listOf(EpisodeDomain(42, "Total Rickall", "S02E04", "August 30, 2015"))
        )

        viewModel.getCharacterDetails(id)

        viewModel.uiState.test {
            val state = expectMostRecentItem() as CharacterDetailsUiState.Success
            assertEquals("Total Rickall", (state.episodesState as EpisodesState.Success).episodes[0].name)
        }
    }

    @Test
    fun `GIVEN character id WHEN getCharacterDetails THEN ScreenOpened event is tracked`() = runTest {
        val id = 5
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(
            CharacterDetailsDomain(
                id = id, name = "Morty", status = "Alive", species = "Human", gender = "Male",
                origin = "Earth", location = "Earth", image = "url", episodeUrls = emptyList()
            )
        )

        viewModel.getCharacterDetails(id)

        verify { analytics.track(CharacterDetailsEvent.ScreenOpened("5")) }
    }

    @Test
    fun `GIVEN episodes loaded WHEN getCharacterDetails THEN EpisodesLoaded event is tracked with correct count`() = runTest {
        val id = 1
        val characterDomain = CharacterDetailsDomain(
            id = id, name = "Rick", status = "Alive", species = "Human", gender = "Male",
            origin = "Earth", location = "Earth", image = "url",
            episodeUrls = listOf(
                "https://rickandmortyapi.com/api/episode/1",
                "https://rickandmortyapi.com/api/episode/2"
            )
        )
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(characterDomain)
        coEvery { getEpisodesUseCase(listOf(1, 2)) } returns successOf(
            listOf(
                EpisodeDomain(1, "Pilot", "S01E01", "December 2, 2013"),
                EpisodeDomain(2, "Lawnmower Dog", "S01E02", "December 9, 2013")
            )
        )

        viewModel.getCharacterDetails(id)

        verify { analytics.track(CharacterDetailsEvent.EpisodesLoaded(2)) }
    }

    @Test
    fun `GIVEN episodes fail WHEN getCharacterDetails THEN EpisodesLoadFailed event is tracked`() = runTest {
        val id = 1
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(
            CharacterDetailsDomain(
                id = id, name = "Rick", status = "Alive", species = "Human", gender = "Male",
                origin = "Earth", location = "Earth", image = "url",
                episodeUrls = listOf("https://rickandmortyapi.com/api/episode/1")
            )
        )
        coEvery { getEpisodesUseCase(any()) } returns NetworkResult.NetworkError(RuntimeException("network error"))

        viewModel.getCharacterDetails(id)

        verify { analytics.track(CharacterDetailsEvent.EpisodesLoadFailed) }
    }

    @Test
    fun `GIVEN episodes fail WHEN getCharacterDetails THEN episodes_fetch trace is stopped`() = runTest {
        val id = 1
        coEvery { getCharacterDetailsUseCase(id) } returns successOf(
            CharacterDetailsDomain(
                id = id, name = "Rick", status = "Alive", species = "Human", gender = "Male",
                origin = "Earth", location = "Earth", image = "url",
                episodeUrls = listOf("https://rickandmortyapi.com/api/episode/1")
            )
        )
        coEvery { getEpisodesUseCase(any()) } returns NetworkResult.NetworkError(RuntimeException("network error"))

        viewModel.getCharacterDetails(id)

        verify { performance.stopTrace("episodes_fetch") }
    }
}