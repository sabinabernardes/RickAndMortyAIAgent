package com.bina.character_details.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bina.character_details.presentation.model.CharacterDetailsUiModel
import com.bina.character_details.presentation.model.EpisodeUiModel
import com.bina.character_details.presentation.state.EpisodesState
import com.bina.designsystem.theme.RickAndMortyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
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
}