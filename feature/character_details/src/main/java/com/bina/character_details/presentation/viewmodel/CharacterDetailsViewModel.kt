package com.bina.character_details.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.domain.usecase.GetEpisodesUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.mapper.EpisodeUiMapper
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import com.bina.character_details.presentation.state.EpisodesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val uiMapper: CharacterDetailsUiMapper,
    private val episodeUiMapper: EpisodeUiMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterDetailsUiState>(CharacterDetailsUiState.Loading)
    val uiState: StateFlow<CharacterDetailsUiState> = _uiState

    fun getCharacterDetails(id: Int) {
        viewModelScope.launch {
            _uiState.value = CharacterDetailsUiState.Loading
            try {
                val domain = getCharacterDetailsUseCase(id)
                _uiState.value = CharacterDetailsUiState.Success(uiMapper.map(domain))

                try {
                    val ids = domain.episodeUrls.map { it.substringAfterLast("/").toInt() }
                    val episodes = getEpisodesUseCase(ids).map(episodeUiMapper::map)
                    _uiState.update { state ->
                        if (state is CharacterDetailsUiState.Success) {
                            state.copy(episodesState = EpisodesState.Success(episodes))
                        } else state
                    }
                } catch (e: Exception) {
                    _uiState.update { state ->
                        if (state is CharacterDetailsUiState.Success) {
                            state.copy(episodesState = EpisodesState.Error(e.message))
                        } else state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CharacterDetailsUiState.Error(e.message)
            }
        }
    }
}
