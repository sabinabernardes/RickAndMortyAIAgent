package com.bina.chat.chat.presentation.viewmodel

import app.cash.turbine.test
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.chat.analytics.ChatEvent
import com.bina.chat.chat.domain.model.AgentMessageResult
import com.bina.chat.chat.domain.model.ChatNavigationEvent
import com.bina.chat.chat.domain.model.MessageRole
import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository
import com.bina.chat.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.chat.presentation.mapper.ChatMessageUiMapper
import com.bina.chat.chat.presentation.state.ChatUiState
import com.bina.logging.AppLogger
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val checkModelAvailabilityUseCase: CheckModelAvailabilityUseCase = mockk()
    private val sendMessageUseCase: SendMessageUseCase = mockk()
    private val repository: ChatRepository = mockk(relaxed = true)
    private val uiMapper = ChatMessageUiMapper()
    private val logger = mockk<AppLogger>(relaxed = true)
    private val analytics = mockk<AnalyticsTracker>(relaxed = true)
    private val performance = mockk<PerformanceTracker>(relaxed = true)

    private lateinit var viewModel: ChatViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { performance.stopTrace(any()) } returns 100L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChatViewModel = ChatViewModel(
        checkModelAvailabilityUseCase,
        sendMessageUseCase,
        repository,
        uiMapper,
        logger,
        analytics,
        performance
    )

    @Test
    fun `GIVEN model Available WHEN init THEN state is Conversation with empty messages`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit

        viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is ChatUiState.Conversation)
        assertTrue((state as ChatUiState.Conversation).messages.isEmpty())
        assertFalse(state.isAiTyping)
    }

    @Test
    fun `GIVEN model Unavailable WHEN init THEN state is ModelUnavailable`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Unavailable

        viewModel = createViewModel()

        assertEquals(ChatUiState.ModelUnavailable, viewModel.uiState.value)
    }

    @Test
    fun `GIVEN model Downloadable WHEN init THEN state is ModelDownloadable`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Downloadable

        viewModel = createViewModel()

        assertEquals(ChatUiState.ModelDownloadable, viewModel.uiState.value)
    }

    @Test
    fun `GIVEN model Available WHEN sendMessage THEN user message and AI response appear in messages`() = runTest(testDispatcher) {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(text = "Rick é genial.")

        viewModel = createViewModel()
        viewModel.sendMessage("Quem é o Rick?")

        val finalState = viewModel.uiState.value as ChatUiState.Conversation
        assertEquals(2, finalState.messages.size)
        assertEquals(MessageRole.USER, finalState.messages[0].role)
        assertEquals("Rick é genial.", finalState.messages[1].text)
        assertFalse(finalState.messages[1].isStreaming)
        assertFalse(finalState.isAiTyping)
        coVerify(exactly = 1) { sendMessageUseCase("Quem é o Rick?") }
    }

    @Test
    fun `GIVEN use case throws WHEN sendMessage THEN errorMessage is set and placeholder removed`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } throws RuntimeException("Falha de rede")

        viewModel = createViewModel()
        viewModel.sendMessage("pergunta")

        val state = viewModel.uiState.value as ChatUiState.Conversation
        assertFalse(state.isAiTyping)
        assertTrue(state.errorMessage != null)
        assertEquals(1, state.messages.size)
    }

    @Test
    fun `GIVEN Conversation with messages WHEN clearChat THEN messages list is empty`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(text = "resposta")

        viewModel = createViewModel()
        viewModel.sendMessage("oi")
        viewModel.clearChat()

        val state = viewModel.uiState.value as ChatUiState.Conversation
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isAiTyping)
    }

    @Test
    fun `GIVEN blank message WHEN sendMessage THEN state does not change`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit

        viewModel = createViewModel()
        val stateBefore = viewModel.uiState.value

        viewModel.sendMessage("   ")

        assertEquals(stateBefore, viewModel.uiState.value)
        coVerify(exactly = 0) { sendMessageUseCase(any()) }
    }

    @Test
    fun `GIVEN result with navigation event WHEN sendMessage THEN navigationEvent is emitted`() = runTest(testDispatcher) {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        val navEvent = ChatNavigationEvent.OpenCharacter(characterId = 42)
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(text = "Aqui está!", navigationEvent = navEvent)

        viewModel = createViewModel()

        viewModel.navigationEvent.test {
            viewModel.sendMessage("Mostre o Rick")
            assertEquals(navEvent, awaitItem())
        }
    }

    @Test
    fun `GIVEN error present WHEN dismissError THEN errorMessage is null`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } throws RuntimeException("erro")

        viewModel = createViewModel()
        viewModel.sendMessage("pergunta")
        viewModel.dismissError()

        val state = viewModel.uiState.value as ChatUiState.Conversation
        assertNull(state.errorMessage)
    }

    @Test
    fun `GIVEN model Unavailable WHEN init THEN ModelUnavailable event is tracked`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Unavailable

        viewModel = createViewModel()

        verify { analytics.track(ChatEvent.ModelUnavailable) }
    }

    @Test
    fun `WHEN sendMessage THEN MessageSent event is tracked`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(text = "resposta")

        viewModel = createViewModel()
        viewModel.sendMessage("olá")

        verify { analytics.track(ChatEvent.MessageSent) }
    }

    @Test
    fun `GIVEN OpenCharacter navigation event WHEN sendMessage THEN AgentNavigationTriggered with open_character is tracked`() = runTest(testDispatcher) {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(
            text = "Aqui está!",
            navigationEvent = ChatNavigationEvent.OpenCharacter(characterId = 1)
        )

        viewModel = createViewModel()
        viewModel.sendMessage("Mostre o Rick")

        verify { analytics.track(ChatEvent.AgentNavigationTriggered("open_character")) }
    }

    @Test
    fun `GIVEN SearchCharacters navigation event WHEN sendMessage THEN AgentNavigationTriggered with search_characters is tracked`() = runTest(testDispatcher) {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } returns AgentMessageResult(
            text = "Buscando!",
            navigationEvent = ChatNavigationEvent.SearchCharacters(query = "Morty")
        )

        viewModel = createViewModel()
        viewModel.sendMessage("busca Morty")

        verify { analytics.track(ChatEvent.AgentNavigationTriggered("search_characters")) }
    }

    @Test
    fun `GIVEN send message fails WHEN sendMessage THEN chat_response_time trace is stopped`() = runTest {
        coEvery { checkModelAvailabilityUseCase() } returns ModelAvailability.Available
        coEvery { repository.warmup() } returns Unit
        coEvery { sendMessageUseCase(any()) } throws RuntimeException("timeout")

        viewModel = createViewModel()
        viewModel.sendMessage("pergunta")

        verify { performance.stopTrace("chat_response_time") }
    }
}