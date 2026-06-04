package com.bina.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
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
class DialogErrorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Behavior ---

    @Test
    fun `GIVEN error message WHEN rendered THEN message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                DialogError(message = "Erro de conexão", onDismiss = {}, onRetry = {})
            }
        }
        composeTestRule.onNodeWithText("Erro de conexão").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `WHEN retry button clicked THEN onRetry callback is triggered`() {
        var retried = false
        composeTestRule.setContent {
            RickAndMortyTheme {
                DialogError(message = "Erro", onDismiss = {}, onRetry = { retried = true })
            }
        }
        composeTestRule.onNodeWithText("Tentar novamente", substring = true).performClick()
        assertTrue(retried)
    }

    @Test
    fun `WHEN dismiss button clicked THEN onDismiss callback is triggered`() {
        var dismissed = false
        composeTestRule.setContent {
            RickAndMortyTheme {
                DialogError(message = "Erro", onDismiss = { dismissed = true }, onRetry = {})
            }
        }
        composeTestRule.onNodeWithText("Fechar").performClick()
        assertTrue(dismissed)
    }

    // --- Dark mode snapshot ---

    @Test
    fun `GIVEN error message dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                DialogError(message = "Erro de conexão", onDismiss = {}, onRetry = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
