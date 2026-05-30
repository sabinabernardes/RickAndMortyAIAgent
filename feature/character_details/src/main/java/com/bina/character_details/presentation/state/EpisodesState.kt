package com.bina.character_details.presentation.state

import com.bina.character_details.presentation.model.EpisodeUiModel

sealed class EpisodesState {
    object Loading : EpisodesState()
    data class Success(val episodes: List<EpisodeUiModel>) : EpisodesState()
    data class Error(val message: String?) : EpisodesState()
}
