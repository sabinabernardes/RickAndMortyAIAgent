package com.bina.character_details.presentation.mapper

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.presentation.model.EpisodeUiModel

class EpisodeUiMapper {
    fun map(domain: EpisodeDomain): EpisodeUiModel {
        return EpisodeUiModel(
            id = domain.id,
            name = domain.name,
            code = domain.code,
            airDate = domain.airDate
        )
    }
}
