package com.bina.auth.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.bina.auth.presentation.state.LoginUiState
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
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- LoginHeader ---

    @Test
    fun `GIVEN header WHEN rendered THEN app title is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginHeader() }
        }
        composeTestRule.onNodeWithText("Rick & Morty AI").assertIsDisplayed()
    }

    @Test
    fun `GIVEN header WHEN rendered THEN subtitle is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginHeader() }
        }
        composeTestRule.onNodeWithText("Entre para continuar").assertIsDisplayed()
    }

    // --- StudyBanner ---

    @Test
    fun `GIVEN study banner WHEN rendered THEN study label is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StudyBanner() }
        }
        composeTestRule.onNodeWithText("Autenticação simulada — app de estudo", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `GIVEN study banner WHEN rendered THEN instructions text is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { StudyBanner() }
        }
        composeTestRule.onNodeWithText("email válido", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("8 caracteres", substring = true).assertIsDisplayed()
    }

    // --- LoginForm: Idle ---

    @Test
    fun `GIVEN idle state WHEN rendered THEN email field is visible`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Idle, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun `GIVEN idle state WHEN rendered THEN password field is visible`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Idle, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onNodeWithText("Senha").assertIsDisplayed()
    }

    @Test
    fun `GIVEN idle state WHEN rendered THEN entrar button is enabled`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Idle, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    // --- LoginForm: Loading ---

    @Test
    fun `GIVEN loading state WHEN rendered THEN entrar button is disabled`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Loading, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }

    @Test
    fun `GIVEN loading state WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme { LoginForm(uiState = LoginUiState.Loading, onLoginClicked = { _, _ -> }) }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // --- LoginForm: Error ---

    @Test
    fun `GIVEN error state WHEN rendered THEN error message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                LoginForm(
                    uiState = LoginUiState.Error("Email inválido."),
                    onLoginClicked = { _, _ -> }
                )
            }
        }
        composeTestRule.onNodeWithText("Email inválido.").assertIsDisplayed()
    }

    @Test
    fun `GIVEN weak password error WHEN rendered THEN error message mentions 8 caracteres`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                LoginForm(
                    uiState = LoginUiState.Error("A senha deve ter no mínimo 8 caracteres."),
                    onLoginClicked = { _, _ -> }
                )
            }
        }
        composeTestRule.onNodeWithText("8 caracteres", substring = true).assertIsDisplayed()
    }

    @Test
    fun `GIVEN error state WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                LoginForm(
                    uiState = LoginUiState.Error("Email inválido."),
                    onLoginClicked = { _, _ -> }
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // --- Dark mode snapshots ---

    @Test
    fun `GIVEN idle state dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                LoginForm(uiState = LoginUiState.Idle, onLoginClicked = { _, _ -> })
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN study banner dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) { StudyBanner() }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
