package com.bina.home.presentation.mapper

import com.bina.home.domain.model.CharacterDomain
import com.bina.home.presentation.model.CharacterUiModel

class CharacterUiMapper {
    fun map(domain: CharacterDomain): CharacterUiModel {
        return CharacterUiModel(
            id = domain.id,
            name = domain.name,
            status = domain.status,
            imageUrl = domain.image
        )
    }
}