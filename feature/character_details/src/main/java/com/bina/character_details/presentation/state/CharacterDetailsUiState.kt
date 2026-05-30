package com.bina.character_details.presentation.state

import com.bina.character_details.presentation.model.CharacterDetailsUiModel

sealed class CharacterDetailsUiState {
    object Loading : CharacterDetailsUiState()
    data class Success(
        val character: CharacterDetailsUiModel,
        val episodesState: EpisodesState = EpisodesState.Loading
    ) : CharacterDetailsUiState()
    data class Error(val message: String?) : CharacterDetailsUiState()
}
