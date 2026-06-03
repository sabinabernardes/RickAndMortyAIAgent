package com.bina.designsystem.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bina.designsystem.theme.RickAndMortyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CardCharacterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val placeholder = object : Painter() {
        override val intrinsicSize = Size.Unspecified
        override fun DrawScope.onDraw() {}
    }

    @Test
    fun `GIVEN character data WHEN rendered THEN name is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Earth (C-137)"
                )
            }
        }
        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
    }

    @Test
    fun `GIVEN character data WHEN rendered THEN species is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Earth (C-137)"
                )
            }
        }
        composeTestRule.onNodeWithText("Human").assertIsDisplayed()
    }

    @Test
    fun `GIVEN character data WHEN rendered THEN location is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Morty Smith",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Citadel of Ricks"
                )
            }
        }
        composeTestRule.onNodeWithText("Citadel of Ricks").assertIsDisplayed()
    }

    // --- Accessibility ---

    @Test
    fun `GIVEN card WHEN rendered THEN node has role Button`() {
        val hasButtonRole = SemanticsMatcher("has Role Button") {
            it.config.contains(SemanticsProperties.Role) && it.config[SemanticsProperties.Role] == Role.Button
        }
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Earth (C-137)"
                )
            }
        }
        composeTestRule.onNode(hasButtonRole).assertExists()
    }

    @Test
    fun `GIVEN card WHEN rendered THEN content description contains character name`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Earth (C-137)"
                )
            }
        }
        composeTestRule.onNode(hasContentDescription("Rick Sanchez", substring = true)).assertExists()
    }

    @Test
    fun `GIVEN card WHEN clicked THEN callback is triggered`() {
        var clicked = false
        composeTestRule.setContent {
            RickAndMortyTheme {
                CardCharacter(
                    painter = placeholder,
                    name = "Rick Sanchez",
                    status = "Alive",
                    species = "Human",
                    lastLocation = "Earth",
                    onClick = { clicked = true }
                )
            }
        }
        composeTestRule.onNodeWithText("Rick Sanchez").performClick()
        assertTrue(clicked)
    }
}
