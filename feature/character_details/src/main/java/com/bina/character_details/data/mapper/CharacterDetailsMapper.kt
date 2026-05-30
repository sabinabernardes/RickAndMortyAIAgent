package com.bina.character_details.data.mapper

import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.domain.model.CharacterDetailsDomain

object CharacterDetailsMapper {
    fun toDomain(data: CharacterDetailsData): CharacterDetailsDomain {
        return CharacterDetailsDomain(
            id = data.id,
            name = data.name,
            status = data.status,
            species = data.species,
            gender = data.gender,
            origin = data.origin.name,
            location = data.location.name,
            image = data.image,
            episodeUrls = data.episode
        )
    }
}
