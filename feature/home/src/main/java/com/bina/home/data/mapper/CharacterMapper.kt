package com.bina.home.data.mapper

import com.bina.home.data.model.CharacterData
import com.bina.home.domain.model.CharacterDomain

object CharacterMapper {
    fun toDomain(data: CharacterData): CharacterDomain {
        return CharacterDomain(
            id = data.id,
            name = data.name,
            status = data.status,
            species = data.species,
            image = data.image,
            location = data.location.name
        )
    }
}
