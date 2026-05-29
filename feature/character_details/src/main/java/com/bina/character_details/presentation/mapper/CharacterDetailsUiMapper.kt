package com.bina.character_details.presentation.mapper

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.presentation.model.CharacterDetailsUiModel

class CharacterDetailsUiMapper {
    fun map(domain: CharacterDetailsDomain): CharacterDetailsUiModel {
        return CharacterDetailsUiModel(
            id = domain.id,
            name = domain.name,
            status = domain.status,
            species = domain.species,
            gender = domain.gender,
            origin = domain.origin,
            location = domain.location,
            imageUrl = domain.image
        )
    }
}
