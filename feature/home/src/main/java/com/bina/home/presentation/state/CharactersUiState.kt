package com.bina.home.presentation.state

import androidx.paging.PagingData
import com.bina.home.presentation.model.CharacterUiModel
import kotlinx.coroutines.flow.Flow

sealed class CharactersUiState {
    object Loading : CharactersUiState()
    data class Success(val data: Flow<PagingData<CharacterUiModel>>) : CharactersUiState()
    data class Error(val message: String?) : CharactersUiState()
}