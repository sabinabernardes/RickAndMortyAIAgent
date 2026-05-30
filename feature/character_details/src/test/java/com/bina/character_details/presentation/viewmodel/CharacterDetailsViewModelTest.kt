package com.bina.character_details.presentation.viewmodel

import app.cash.turbine.test
import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.domain.usecase.GetEpisodesUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.mapper.EpisodeUiMapper
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import io.mockk.coEvery
import io.mockk.mockk
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
    private lateinit var viewModel: CharacterDetailsViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CharacterDetailsViewModel(getCharacterDetailsUseCase, getEpisodesUseCase, uiMapper, episodeUiMapper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `GIVEN a character id WHEN getCharacterDetails is called THEN should emit success state`() = runTest {
        // GIVEN
        val id = 1
        val characterDomain = CharacterDetailsDomain(
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
        coEvery { getCharacterDetailsUseCase(id) } returns characterDomain

        // WHEN
        viewModel.getCharacterDetails(id)

        // THEN
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assert(state is CharacterDetailsUiState.Success)
            assertEquals(characterDomain.name, (state as CharacterDetailsUiState.Success).character.name)
        }
    }

    @Test
    fun `GIVEN a character id WHEN getCharacterDetails fails THEN should emit error state`() = runTest {
        // GIVEN
        val id = 1
        val errorMessage = "Error loading character"
        coEvery { getCharacterDetailsUseCase(id) } throws Exception(errorMessage)

        // WHEN
        viewModel.getCharacterDetails(id)

        // THEN
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assert(state is CharacterDetailsUiState.Error)
            assertEquals(errorMessage, (state as CharacterDetailsUiState.Error).message)
        }
    }
}
