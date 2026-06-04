package com.bina.chat.chat.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.bina.chat.chat.domain.model.MessageRole
import com.bina.chat.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.chat.presentation.state.ChatUiState
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
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- ModelUnavailable ---

    @Test
    fun `GIVEN model unavailable state WHEN rendered THEN error title is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { ModelUnavailableContent() }
        }
        composeTestRule.onNodeWithText("Erro ao carregar modelo").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN model unavailable state WHEN rendered THEN description message is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme { ModelUnavailableContent() }
        }
        composeTestRule.onNodeWithText("Não foi possível inicializar o Rick AI.", substring = true).assertIsDisplayed()
    }

    // --- ConversationContent: empty ---

    @Test
    fun `GIVEN empty conversation WHEN rendered THEN empty state title is displayed`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = emptyList(), isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Fale com o Rick").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    // --- ConversationContent: messages ---

    @Test
    fun `GIVEN conversation with messages WHEN rendered THEN user message is displayed`() {
        val messages = listOf(
            ChatMessageUiModel(role = MessageRole.USER, text = "Quem é o Rick?")
        )
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = messages, isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Quem é o Rick?").assertIsDisplayed()
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN conversation with messages WHEN rendered THEN AI message is displayed`() {
        val messages = listOf(
            ChatMessageUiModel(role = MessageRole.AI, text = "Sou o cientista mais inteligente do universo.")
        )
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = messages, isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNodeWithText("Sou o cientista mais inteligente do universo.").assertIsDisplayed()
    }

    // --- Accessibility ---

    @Test
    fun `GIVEN ai is typing WHEN rendered THEN send button announces ai typing`() {
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = emptyList(), isAiTyping = true),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNode(hasContentDescription("Rick AI está digitando", substring = true)).assertExists()
    }

    @Test
    fun `GIVEN user message WHEN rendered THEN content description contains sender prefix`() {
        val messages = listOf(
            ChatMessageUiModel(role = MessageRole.USER, text = "Olá Rick")
        )
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = messages, isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNode(hasContentDescription("Você: Olá Rick", substring = true)).assertExists()
    }

    @Test
    fun `GIVEN ai message WHEN rendered THEN content description contains sender prefix`() {
        val messages = listOf(
            ChatMessageUiModel(role = MessageRole.AI, text = "Wubba lubba dub dub!")
        )
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = messages, isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onNode(hasContentDescription("Rick AI: Wubba lubba dub dub!", substring = true)).assertExists()
    }

    @Test
    fun `GIVEN typed message WHEN send clicked THEN onSendMessage callback fires`() {
        var sentMessage = ""
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = emptyList(), isAiTyping = false),
                    onSendMessage = { sentMessage = it },
                )
            }
        }
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Oi Rick")
        composeTestRule.onNodeWithContentDescription("Enviar").performClick()
        assertTrue(sentMessage == "Oi Rick")
    }

    // --- Dark mode snapshots ---

    @Test
    fun `GIVEN empty conversation dark mode WHEN rendered THEN matches snapshot`() {
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = emptyList(), isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun `GIVEN conversation with messages dark mode WHEN rendered THEN matches snapshot`() {
        val messages = listOf(
            ChatMessageUiModel(role = MessageRole.USER, text = "Quem é o Rick?"),
            ChatMessageUiModel(role = MessageRole.AI, text = "Sou o cientista mais inteligente do universo.")
        )
        composeTestRule.setContent {
            RickAndMortyTheme(useDarkTheme = true) {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = messages, isAiTyping = false),
                    onSendMessage = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
