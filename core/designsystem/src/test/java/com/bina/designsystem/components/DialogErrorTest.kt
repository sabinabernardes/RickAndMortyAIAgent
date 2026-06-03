package com.bina.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
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
class DialogErrorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN error message WHEN rendered THEN message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                DialogError(
                    message = "Erro de conexão",
                    onDismiss = {},
                    onRetry = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Erro de conexão").assertIsDisplayed()
    }

    @Test
    fun `WHEN retry button clicked THEN onRetry callback is triggered`() {
        var retried = false
        composeTestRule.setContent {
            RickAndMortyTheme {
                DialogError(
                    message = "Erro",
                    onDismiss = {},
                    onRetry = { retried = true }
                )
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
                DialogError(
                    message = "Erro",
                    onDismiss = { dismissed = true },
                    onRetry = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Fechar").performClick()
        assertTrue(dismissed)
    }
}
