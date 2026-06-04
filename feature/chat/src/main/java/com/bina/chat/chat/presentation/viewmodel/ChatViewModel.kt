package com.bina.chat.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.chat.analytics.ChatEvent
import com.bina.chat.chat.domain.model.ChatNavigationEvent
import com.bina.chat.chat.domain.model.MessageRole
import com.bina.chat.chat.domain.model.ModelAvailability
import com.bina.chat.chat.domain.repository.ChatRepository
import com.bina.chat.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.chat.presentation.state.ChatUiState
import com.bina.logging.AppLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val checkModelAvailabilityUseCase: CheckModelAvailabilityUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val repository: ChatRepository,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initializing)
    val uiState: StateFlow<ChatUiState> = _uiState

    private val _navigationEvent = MutableSharedFlow<ChatNavigationEvent>()
    val navigationEvent: SharedFlow<ChatNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch { checkAvailability() }
    }

    fun checkAvailability() {
        viewModelScope.launch { doCheckAvailability() }
    }

    private suspend fun doCheckAvailability() {
        logger.debug(TAG, "checking model availability")
        when (checkModelAvailabilityUseCase()) {
            is ModelAvailability.Available -> {
                logger.info(TAG, "model available")
                _uiState.value = ChatUiState.Conversation(
                    messages = emptyList(),
                    isAiTyping = false
                )
            }
            is ModelAvailability.Unavailable -> {
                logger.warn(TAG, "model unavailable")
                analytics.track(ChatEvent.ModelUnavailable)
                _uiState.value = ChatUiState.ModelUnavailable
            }
            is ModelAvailability.Downloadable -> {
                logger.info(TAG, "model downloadable")
                _uiState.value = ChatUiState.ModelDownloadable
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val currentState = _uiState.value as? ChatUiState.Conversation ?: return

        analytics.track(ChatEvent.MessageSent)
        logger.debug(TAG, "sending message length=${userText.length}")

        val userMessage = ChatMessageUiModel(role = MessageRole.USER, text = userText)
        val placeholder = ChatMessageUiModel(role = MessageRole.AI, text = "", isStreaming = true)

        _uiState.value = currentState.copy(
            messages = currentState.messages + userMessage + placeholder,
            isAiTyping = true,
            errorMessage = null
        )

        viewModelScope.launch {
            performance.startTrace(TRACE_RESPONSE_TIME)
            try {
                val result = sendMessageUseCase(userText)
                val duration = performance.stopTrace(TRACE_RESPONSE_TIME)
                logger.info(TAG, "response received in ${duration}ms")

                val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
                val finalMessages = state.messages.toMutableList().also {
                    it[it.lastIndex] = placeholder.copy(text = result.text, isStreaming = false)
                }
                _uiState.value = state.copy(messages = finalMessages, isAiTyping = false)

                result.navigationEvent?.let { event ->
                    val action = when (event) {
                        is ChatNavigationEvent.OpenCharacter -> "open_character"
                        is ChatNavigationEvent.SearchCharacters -> "search_characters"
                    }
                    analytics.track(ChatEvent.AgentNavigationTriggered(action))
                    _navigationEvent.emit(event)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                performance.stopTrace(TRACE_RESPONSE_TIME)
                logger.error(TAG, "send message failed", e)
                val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
                _uiState.value = state.copy(
                    messages = state.messages.dropLast(1),
                    isAiTyping = false,
                    errorMessage = "Erro ao gerar resposta. Tente novamente."
                )
            }
        }
    }

    fun clearChat() {
        val currentState = _uiState.value as? ChatUiState.Conversation ?: return
        _uiState.value = currentState.copy(messages = emptyList(), isAiTyping = false, errorMessage = null)
    }

    fun dismissError() {
        val currentState = _uiState.value as? ChatUiState.Conversation ?: return
        _uiState.value = currentState.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }

    companion object {
        private const val TAG = "ChatViewModel"
        private const val TRACE_RESPONSE_TIME = "chat_response_time"
    }
}
