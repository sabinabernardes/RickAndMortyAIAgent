package com.bina.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.bina.designsystem.theme.RickAndMortyTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StatusBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Behavior ---

    @Test
    fun `GIVEN alive status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN dead status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Dead") }
        }
        composeTestRule.onNodeWithText("Dead").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN unknown status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "unknown") }
        }
        composeTestRule.onNodeWithText("unknown").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    // --- Dark mode snapshots ---

    @Test
    fun `GIVEN alive status dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) { StatusBadge(status = "Alive") }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN dead status dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) { StatusBadge(status = "Dead") }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN unknown status dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) { StatusBadge(status = "unknown") }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
