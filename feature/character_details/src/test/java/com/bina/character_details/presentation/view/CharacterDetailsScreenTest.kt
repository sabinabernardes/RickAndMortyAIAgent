package com.bina.character_details.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.bina.character_details.presentation.model.CharacterDetailsUiModel
import com.bina.character_details.presentation.model.EpisodeUiModel
import com.bina.character_details.presentation.state.EpisodesState
import com.bina.designsystem.theme.RickAndMortyTheme
import com.github.takahirom.roborazzi.captureRoboImage
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
class CharacterDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val character = CharacterDetailsUiModel(
        id = 1,
        name = "Rick Sanchez",
        status = "Alive",
        species = "Human",
        gender = "Male",
        origin = "Earth (C-137)",
        location = "Citadel of Ricks",
        imageUrl = ""
    )

    // --- Behavior ---

    @Test
    fun `GIVEN success state WHEN rendered THEN character name is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Loading,
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onAllNodesWithText("Rick Sanchez")[0].assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN success state WHEN rendered THEN species and gender are displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Loading,
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Human · Male").assertIsDisplayed()
    }

    @Test
    fun `GIVEN success state with episodes WHEN rendered THEN episode name is displayed`() {
        val episodes = listOf(
            EpisodeUiModel(id = 1, name = "Pilot", code = "S01E01", airDate = "December 2, 2013")
        )
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Success(episodes),
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Pilot").assertExists()
        composeTestRule.onNodeWithText("S01E01").assertExists()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN success state WHEN back button clicked THEN callback is triggered`() {
        var backClicked = false
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Loading,
                    onBackClick = { backClicked = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Voltar").performClick()
        assertTrue(backClicked)
    }

    // --- Accessibility ---

    @Test
    fun `GIVEN success state WHEN rendered THEN detail item label and value are merged in single node`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Loading,
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onNode(
            hasText("Status", substring = true) and hasText("Alive", substring = true)
        ).assertExists()
    }

    @Test
    fun `GIVEN episodes error state WHEN rendered THEN error message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Error("Falha ao carregar"),
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Não foi possível carregar os episódios.").assertExists()
    }

    // --- Dark mode snapshot ---

    @Test
    fun `GIVEN character details dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                CharacterDetailsContent(
                    character = character,
                    episodesState = EpisodesState.Loading,
                    onBackClick = {}
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
