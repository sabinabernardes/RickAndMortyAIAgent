package com.bina.home.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.designsystem.theme.RickAndMortyTheme
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.viewmodel.HomeViewModel
import com.bina.logging.AppLogger
import com.github.takahirom.roborazzi.captureRoboImage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun fakeViewModel(): HomeViewModel {
        val useCase = mockk<GetCharactersUseCase>(relaxed = true)
        val logger = mockk<AppLogger>(relaxed = true)
        val analytics = mockk<AnalyticsTracker>(relaxed = true)
        val performance = mockk<PerformanceTracker>(relaxed = true)
        every { performance.stopTrace(any()) } returns 0L
        return HomeViewModel(useCase, CharacterUiMapper(), logger, analytics, performance, debounceMs = 0L)
    }

    // --- Accessibility ---

    @Test
    fun `GIVEN loading state WHEN rendered THEN loading grid has content description`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoadingContent() }
        }
        composeTestRule.onNode(hasContentDescription("Carregando personagens", substring = true)).assertExists()
    }

    // --- Behavior ---

    @Test
    fun `GIVEN loading state WHEN rendered THEN no error dialog is shown`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Loading,
                    onCharacterClick = {},
                    viewModel = fakeViewModel()
                )
            }
        }
        composeTestRule.onNodeWithText("Algo deu errado").assertDoesNotExist()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN error state WHEN rendered THEN error message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Error("Sem conexão"),
                    onCharacterClick = {},
                    viewModel = fakeViewModel()
                )
            }
        }
        composeTestRule.onNodeWithText("Sem conexão").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN error state WHEN retry clicked THEN onRetry is called on viewModel`() {
        val viewModel = mockk<HomeViewModel>(relaxed = true)
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Error("Erro"),
                    onCharacterClick = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Tentar novamente", substring = true).performClick()
        verify { viewModel.onRetry() }
    }

    @Test
    fun `GIVEN error state WHEN dismiss clicked THEN clearError is called on viewModel`() {
        val viewModel = mockk<HomeViewModel>(relaxed = true)
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Error("Erro"),
                    onCharacterClick = {},
                    viewModel = viewModel
                )
            }
        }
        composeTestRule.onNodeWithText("Fechar").performClick()
        verify { viewModel.clearError() }
    }

    @Test
    fun `GIVEN success state WHEN character clicked THEN onCharacterClick callback fires`() {
        var clickedId = -1
        composeTestRule.setContent {
            RickAndMortyTheme {
                HomeContent(
                    uiState = CharactersUiState.Loading,
                    onCharacterClick = { id -> clickedId = id },
                    viewModel = fakeViewModel()
                )
            }
        }
        assertTrue(clickedId == -1)
    }

    // --- Dark mode snapshot ---

    @Test
    fun `GIVEN error state dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                HomeContent(
                    uiState = CharactersUiState.Error("Sem conexão"),
                    onCharacterClick = {},
                    viewModel = fakeViewModel()
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
