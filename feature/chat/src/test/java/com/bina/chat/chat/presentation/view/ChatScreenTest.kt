package com.bina.chat.chat.presentation.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.bina.chat.chat.domain.model.MessageRole
import com.bina.chat.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.chat.presentation.state.ChatUiState
import com.bina.designsystem.theme.RickAndMortyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
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
                    onDismissError = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Fale com o Rick").assertIsDisplayed()
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
                    onDismissError = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Quem é o Rick?").assertIsDisplayed()
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
                    onDismissError = {}
                )
            }
        }
        composeTestRule.onNodeWithText("Sou o cientista mais inteligente do universo.").assertIsDisplayed()
    }

    // --- ConversationContent: send message ---

    @Test
    fun `GIVEN typed message WHEN send clicked THEN onSendMessage callback fires`() {
        var sentMessage = ""
        composeTestRule.setContent {
            RickAndMortyTheme {
                ConversationContent(
                    state = ChatUiState.Conversation(messages = emptyList(), isAiTyping = false),
                    onSendMessage = { sentMessage = it },
                    onDismissError = {}
                )
            }
        }
        composeTestRule.onNode(hasSetTextAction()).performTextInput("Oi Rick")
        composeTestRule.onNodeWithContentDescription("Enviar").performClick()
        assertTrue(sentMessage == "Oi Rick")
    }
}