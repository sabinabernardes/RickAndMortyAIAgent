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
import com.bina.logging.AppLogger
import com.bina.network.NetworkResult
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
            performance.startTrace(TRACE_DETAILS_LOAD)

            when (val result = getCharacterDetailsUseCase(id)) {
                is NetworkResult.Success -> {
                    val duration = performance.stopTrace(TRACE_DETAILS_LOAD)
                    logger.info(TAG, "character $id loaded in ${duration}ms")
                    _uiState.value = CharacterDetailsUiState.Success(uiMapper.map(result.data))
                    loadEpisodes(result.data)
                }
                is NetworkResult.Error -> {
                    performance.stopTrace(TRACE_DETAILS_LOAD)
                    logger.error(TAG, "character $id load failed", result.exception)
                    _uiState.value = CharacterDetailsUiState.Error(result.exception.message)
                }
                is NetworkResult.Unauthorized -> {
                    performance.stopTrace(TRACE_DETAILS_LOAD)
                    logger.error(TAG, "unauthorized loading character $id")
                    _uiState.value = CharacterDetailsUiState.Error("Acesso não autorizado")
                }
                else -> {
                    performance.stopTrace(TRACE_DETAILS_LOAD)
                    _uiState.value = CharacterDetailsUiState.Error(null)
                }
            }
        }
    }

    private suspend fun loadEpisodes(domain: CharacterDetailsDomain) {
        performance.startTrace(TRACE_EPISODES_FETCH)
        val ids = domain.episodeUrls.map { it.substringAfterLast("/").toInt() }

        when (val result = getEpisodesUseCase(ids)) {
            is NetworkResult.Success -> {
                val episodes = result.data.map(episodeUiMapper::map)
                val duration = performance.stopTrace(TRACE_EPISODES_FETCH)
                logger.info(TAG, "episodes loaded count=${episodes.size} in ${duration}ms")
                analytics.track(CharacterDetailsEvent.EpisodesLoaded(episodes.size))
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Success(episodes))
                    } else {
                        state
                    }
                }
            }
            else -> {
                performance.stopTrace(TRACE_EPISODES_FETCH)
                val error = if (result is NetworkResult.Error) result.exception else RuntimeException("Unknown error")
                logger.warn(TAG, "episodes load failed", error)
                analytics.track(CharacterDetailsEvent.EpisodesLoadFailed)
                _uiState.update { state ->
                    if (state is CharacterDetailsUiState.Success) {
                        state.copy(episodesState = EpisodesState.Error(error.message))
                    } else {
                        state
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "CharacterDetailsViewModel"
        private const val TRACE_DETAILS_LOAD = "character_details_load"
        private const val TRACE_EPISODES_FETCH = "episodes_fetch"
    }
}
