package com.bina.character_details.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.character_details.analytics.CharacterDetailsEvent
import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.usecase.GetCharacterDetailsUseCase
import com.bina.character_details.domain.usecase.GetEpisodesUseCase
import com.bina.character_details.presentation.mapper.CharacterDetailsUiMapper
import com.bina.character_details.presentation.mapper.EpisodeUiMapper
import com.bina.character_details.presentation.state.CharacterDetailsUiState
import com.bina.character_details.presentation.state.EpisodesState
import com.bina.domain.DomainResult
import com.bina.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CharacterDetailsViewModel(
    private val getCharacterDetailsUseCase: GetCharacterDetailsUseCase,
    private val getEpisodesUseCase: GetEpisodesUseCase,
    private val uiMapper: CharacterDetailsUiMapper,
    private val episodeUiMapper: EpisodeUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterDetailsUiState>(CharacterDetailsUiState.Loading)
    val uiState: StateFlow<CharacterDetailsUiState> = _uiState

    fun getCharacterDetails(id: Int) {
        viewModelScope.launch {
            _uiState.value = CharacterDetailsUiState.Loading
            analytics.track(CharacterDetailsEvent.ScreenOpened(id.toString()))
            logger.debug(TAG, "loading character id=$id")

            when (val result = getCharacterDetailsUseCase(id)) {
                is DomainResult.Success -> {
                    logger.info(TAG, "character $id loaded")
                    _uiState.value = CharacterDetailsUiState.Success(uiMapper.map(result.data))
                    loadEpisodes(result.data)
                }
                is DomainResult.Unauthorized -> {
                    logger.error(TAG, "unauthorized loading character $id")
                    _uiState.value = CharacterDetailsUiState.Error("Acesso não autorizado")
                }
                is DomainResult.Error -> {
                    logger.error(TAG, "character $id load failed: ${result.message}")
                    _uiState.value = CharacterDetailsUiState.Error(result.message)
                }
                else -> _uiState.value = CharacterDetailsUiState.Error(null)
            }
        }
    }

    private suspend fun loadEpisodes(domain: CharacterDetailsDomain) {
        val ids = domain.episodeUrls.map { it.substringAfterLast("/").toInt() }
        performance.startTrace("episodes_fetch")
        when (val result = getEpisodesUseCase(ids)) {
            is DomainResult.Success -> {
                performance.stopTrace("episodes_fetch")
                val episodes = result.data.map(episodeUiMapper::map)
                logger.info(TAG, "episodes loaded count=${episodes.size}")
                analytics.track(CharacterDetailsEvent.EpisodesLoaded(episodes.size))
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Success(episodes))
                    } else {
                        state
                    }
                }
            }
            is DomainResult.Error -> {
                performance.stopTrace("episodes_fetch")
                logger.warn(TAG, "episodes load failed: ${result.message}")
                analytics.track(CharacterDetailsEvent.EpisodesLoadFailed)
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Error(result.message))
                    } else {
                        state
                    }
                }
            }
            else -> {
                performance.stopTrace("episodes_fetch")
                logger.warn(TAG, "episodes load failed: resultado inesperado ${result::class.simpleName}")
                analytics.track(CharacterDetailsEvent.EpisodesLoadFailed)
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Error(null))
                    } else {
                        state
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "CharacterDetailsViewModel"
    }
}
