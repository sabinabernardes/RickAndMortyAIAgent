package com.bina.character_details.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase,
    private val uiMapper: CharacterDetailsUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterDetailsUiState>(CharacterDetailsUiState.Loading)
    val uiState: StateFlow<CharacterDetailsUiState> = _uiState

    fun getCharacterDetails(id: Int) {
        viewModelScope.launch {
            _uiState.value = CharacterDetailsUiState.Loading
            try {
                val domain = getCharacterDetailsUseCase(id)
                val uiModel = uiMapper.map(domain)
                _uiState.value = CharacterDetailsUiState.Success(uiModel)
            } catch (e: Exception) {
                _uiState.value = CharacterDetailsUiState.Error(e.message)
            }
        }
    }
}
