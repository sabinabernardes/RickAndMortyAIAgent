package com.bina.home.presentation.state

sealed class CharactersUiState {
    object Loading : CharactersUiState()
    object Success : CharactersUiState()
    data class Error(val message: String?) : CharactersUiState()
}
