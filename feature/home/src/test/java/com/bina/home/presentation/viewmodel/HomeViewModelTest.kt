package com.bina.home.presentation.viewmodel

import androidx.paging.PagingData
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.analytics.event.AnalyticsEvent
import com.bina.home.analytics.HomeEvent
import com.bina.home.domain.model.CharacterDomain
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.state.CharactersUiState
import com.bina.logging.AppLogger
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val getCharactersUseCase: GetCharactersUseCase = mockk()
    private val uiMapper = CharacterUiMapper()
    private val logger = mockk<AppLogger>(relaxed = true)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)
    private val performance = mockk<PerformanceTracker>(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { performance.stopTrace(any()) } returns 100L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun defaultPagingFlow() = flowOf(
        PagingData.from(listOf(CharacterDomain(1, "Rick", "Alive", "Human", "img", "Earth")))
    )

    private fun createViewModel() = HomeViewModel(getCharactersUseCase, uiMapper, logger, analytics, performance)

    @Test
    fun `GIVEN use case returns data WHEN init THEN state is Success`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is CharactersUiState.Success)
    }

    @Test
    fun `GIVEN init WHEN getCharacters THEN use case is called with empty string`() = runTest {
        every { getCharactersUseCase("") } returns defaultPagingFlow()

        viewModel = createViewModel()

        coVerify { getCharactersUseCase("") }
    }

    @Test
    fun `GIVEN successful init WHEN onQueryChange THEN use case is called with new query`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()
        viewModel.onQueryChange("Morty")

        coVerify(atLeast = 1) { getCharactersUseCase("Morty") }
    }

    @Test
    fun `GIVEN query set WHEN onRetry THEN use case is called again with same query`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()
        viewModel.onQueryChange("Rick")
        viewModel.onRetry()

        coVerify(atLeast = 2) { getCharactersUseCase("Rick") }
    }

    @Test
    fun `GIVEN state is not Error WHEN clearError THEN state remains unchanged`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()
        viewModel = createViewModel()
        val stateBefore = viewModel.uiState.value

        viewModel.clearError()

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun `GIVEN multiple queries WHEN onQueryChange THEN each triggers use case with correct query`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()
        viewModel.onQueryChange("Rick")
        viewModel.onQueryChange("Morty")

        coVerify(atLeast = 1) { getCharactersUseCase("Rick") }
        coVerify(atLeast = 1) { getCharactersUseCase("Morty") }
    }

    @Test
    fun `GIVEN non-blank query WHEN onQueryChange THEN SearchPerformed event is tracked`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()
        viewModel.onQueryChange("Rick")

        verify { analytics.track(HomeEvent.SearchPerformed("Rick")) }
    }

    @Test
    fun `GIVEN blank query WHEN onQueryChange THEN SearchPerformed is NOT tracked`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()
        viewModel.onQueryChange("")

        verify(exactly = 0) { analytics.track(ofType<HomeEvent.SearchPerformed>()) }
    }

    @Test
    fun `WHEN onCharacterClicked THEN CharacterClicked event is tracked`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()
        viewModel = createViewModel()

        viewModel.onCharacterClicked(42)

        verify { analytics.track(HomeEvent.CharacterClicked("42")) }
    }

    @Test
    fun `WHEN onPageLoaded THEN PaginationLoadedNextPage event is tracked`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()
        viewModel = createViewModel()

        viewModel.onPageLoaded()

        verify { analytics.track(HomeEvent.PaginationLoadedNextPage) }
    }

    @Test
    fun `GIVEN successful data load WHEN init THEN screen load trace is stopped once`() = runTest {
        every { getCharactersUseCase(any()) } returns defaultPagingFlow()

        viewModel = createViewModel()

        verify(exactly = 1) { performance.stopTrace("home_screen_load") }
    }
}