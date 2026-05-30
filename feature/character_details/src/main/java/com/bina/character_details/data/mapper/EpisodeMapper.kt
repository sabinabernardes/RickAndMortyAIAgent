package com.bina.character_details.data.mapper

import com.bina.character_details.data.model.EpisodeData
import com.bina.character_details.domain.model.EpisodeDomain

object EpisodeMapper {
    fun toDomain(data: EpisodeData): EpisodeDomain {
        return EpisodeDomain(
            id = data.id,
            name = data.name,
            code = data.episode,
            airDate = data.airDate
        )
    }
}
