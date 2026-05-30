package com.bina.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.chat.domain.model.ChatNavigationEvent
import com.bina.chat.domain.model.MessageRole
import com.bina.chat.domain.model.ModelAvailability
import com.bina.chat.domain.repository.ChatRepository
import com.bina.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.presentation.mapper.ChatMessageUiMapper
import com.bina.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.presentation.state.ChatUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val checkModelAvailabilityUseCase: CheckModelAvailabilityUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val repository: ChatRepository,
    private val uiMapper: ChatMessageUiMapper
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
        when (checkModelAvailabilityUseCase()) {
            is ModelAvailability.Available -> {
                _uiState.value = ChatUiState.Conversation(
                    messages = emptyList(),
                    isAiTyping = false
                )
            }
            is ModelAvailability.Unavailable -> _uiState.value = ChatUiState.ModelUnavailable
            is ModelAvailability.Downloadable -> _uiState.value = ChatUiState.ModelDownloadable
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val currentState = _uiState.value as? ChatUiState.Conversation ?: return

        val userMessage = ChatMessageUiModel(role = MessageRole.USER, text = userText)
        val placeholder = ChatMessageUiModel(role = MessageRole.AI, text = "", isStreaming = true)

        _uiState.value = currentState.copy(
            messages = currentState.messages + userMessage + placeholder,
            isAiTyping = true,
            errorMessage = null
        )

        viewModelScope.launch {
            try {
                val result = sendMessageUseCase(userText)
                val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
                val finalMessages = state.messages.toMutableList().also {
                    it[it.lastIndex] = placeholder.copy(text = result.text, isStreaming = false)
                }
                _uiState.value = state.copy(messages = finalMessages, isAiTyping = false)
                result.navigationEvent?.let { _navigationEvent.emit(it) }
            } catch (e: Exception) {
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
}
