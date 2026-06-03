package com.bina.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.bina.analytics.AnalyticsTracker
import com.bina.analytics.PerformanceTracker
import com.bina.home.analytics.HomeEvent
import com.bina.home.domain.model.CharacterDomain
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.model.CharacterUiModel
import com.bina.logging.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

open class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper,
    private val logger: AppLogger,
    private val analytics: AnalyticsTracker,
    private val performance: PerformanceTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow<CharactersUiState>(CharactersUiState.Loading)
    val uiState: StateFlow<CharactersUiState> = _uiState

    private var currentQuery = ""
    private var screenLoadTracked = false

    init {
        logger.debug(TAG, "initialized")
        performance.startTrace(TRACE_SCREEN_LOAD)
        getCharacters()
    }

    fun getCharacters(query: String = "") {
        currentQuery = query
        if (query.isNotBlank()) analytics.track(HomeEvent.SearchPerformed(query))
        viewModelScope.launch {
            getCharactersUseCase(query)
                .map { pagingData: PagingData<CharacterDomain> ->
                    pagingData.map { domain: CharacterDomain ->
                        uiMapper.map(domain)
                    }
                }
                .cachedIn(viewModelScope)
                .onStart {
                    logger.debug(TAG, "loading characters query='$query'")
                    _uiState.value = CharactersUiState.Loading
                }
                .catch { e ->
                    logger.error(TAG, "characters load failed", e)
                    _uiState.value = CharactersUiState.Error(e.message)
                }
                .collect { mappedPagingData: PagingData<CharacterUiModel> ->
                    if (!screenLoadTracked) {
                        val duration = performance.stopTrace(TRACE_SCREEN_LOAD)
                        logger.info(TAG, "home_screen_load: ${duration}ms")
                        screenLoadTracked = true
                    }
                    _uiState.value = CharactersUiState.Success(flowOf(mappedPagingData))
                }
        }
    }

    fun onCharacterClicked(characterId: Int) {
        analytics.track(HomeEvent.CharacterClicked(characterId.toString()))
    }

    fun onPageLoaded() {
        analytics.track(HomeEvent.PaginationLoadedNextPage)
    }

    fun onQueryChange(newQuery: String) {
        getCharacters(newQuery)
    }

    fun onRetry() {
        getCharacters(currentQuery)
    }

    fun clearError() {
        if (_uiState.value is CharactersUiState.Error) {
            _uiState.value = CharactersUiState.Loading
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val TRACE_SCREEN_LOAD = "home_screen_load"
    }
}