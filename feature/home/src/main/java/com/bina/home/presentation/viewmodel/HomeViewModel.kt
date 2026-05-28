package com.bina.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.bina.home.domain.model.CharacterDomain
import com.bina.home.domain.usecase.GetCharactersUseCase
import com.bina.home.presentation.state.CharactersUiState
import com.bina.home.presentation.mapper.CharacterUiMapper
import com.bina.home.presentation.model.CharacterUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

open class HomeViewModel(
    private val getCharactersUseCase: GetCharactersUseCase,
    private val uiMapper: CharacterUiMapper
) : ViewModel() {
    private val _uiState = MutableStateFlow<CharactersUiState>(CharactersUiState.Loading)
    val uiState: StateFlow<CharactersUiState> = _uiState

    private var currentQuery = ""

    fun getCharacters(query: String = "") {
        currentQuery = query
        viewModelScope.launch {
            getCharactersUseCase(query)
                .map { pagingData: PagingData<CharacterDomain> ->
                    pagingData.map { domain: CharacterDomain ->
                        uiMapper.map(domain)
                    }
                }
                .cachedIn(viewModelScope)
                .onStart {
                    _uiState.value = CharactersUiState.Loading
                }
                .catch { e ->
                    _uiState.value = CharactersUiState.Error(e.message)
                }
                .collect { mappedPagingData: PagingData<CharacterUiModel> ->
                    _uiState.value = CharactersUiState.Success(flowOf(mappedPagingData))
                }
        }
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
}