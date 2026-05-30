package com.bina.chat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.chat.domain.model.MessageRole
import com.bina.chat.domain.model.ModelAvailability
import com.bina.chat.domain.repository.ChatRepository

import com.bina.chat.domain.usecase.CheckModelAvailabilityUseCase
import com.bina.chat.domain.usecase.SendMessageUseCase
import com.bina.chat.presentation.mapper.ChatMessageUiMapper
import com.bina.chat.presentation.model.ChatMessageUiModel
import com.bina.chat.presentation.state.ChatUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val checkModelAvailabilityUseCase: CheckModelAvailabilityUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val repository: ChatRepository,
    private val uiMapper: ChatMessageUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Initializing)
    val uiState: StateFlow<ChatUiState> = _uiState

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
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return
        val currentState = _uiState.value as? ChatUiState.Conversation ?: return

        val userMessage = ChatMessageUiModel(role = MessageRole.USER, text = userText)
        val streamingPlaceholder = ChatMessageUiModel(role = MessageRole.AI, text = "", isStreaming = true)

        _uiState.value = currentState.copy(
            messages = currentState.messages + userMessage + streamingPlaceholder,
            isAiTyping = true,
            errorMessage = null
        )

        viewModelScope.launch {
            var accumulated = ""
            try {
                sendMessageUseCase(userText).collect { chunk ->
                    accumulated += chunk
                    val state = _uiState.value as? ChatUiState.Conversation ?: return@collect
                    val updatedMessages = state.messages.toMutableList().also {
                        it[it.lastIndex] = streamingPlaceholder.copy(text = accumulated)
                    }
                    _uiState.value = state.copy(messages = updatedMessages)
                }
                val finalState = _uiState.value as? ChatUiState.Conversation ?: return@launch
                val finalMessages = finalState.messages.toMutableList().also {
                    it[it.lastIndex] = it.last().copy(isStreaming = false)
                }
                _uiState.value = finalState.copy(messages = finalMessages, isAiTyping = false)
            } catch (e: Exception) {
                val state = _uiState.value as? ChatUiState.Conversation ?: return@launch
                val messagesWithoutPlaceholder = state.messages.dropLast(1)
                _uiState.value = state.copy(
                    messages = messagesWithoutPlaceholder,
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
