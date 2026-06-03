package com.bina.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.bina.designsystem.theme.RickAndMortyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class StatusBadgeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `GIVEN alive status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Alive") }
        }
        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
    }

    @Test
    fun `GIVEN dead status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "Dead") }
        }
        composeTestRule.onNodeWithText("Dead").assertIsDisplayed()
    }

    @Test
    fun `GIVEN unknown status WHEN rendered THEN text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StatusBadge(status = "unknown") }
        }
        composeTestRule.onNodeWithText("unknown").assertIsDisplayed()
    }
}
