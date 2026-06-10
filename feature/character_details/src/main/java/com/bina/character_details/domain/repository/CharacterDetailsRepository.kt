package com.bina.character_details.domain.repository

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.domain.DomainResult

interface CharacterDetailsRepository {
    suspend fun getCharacterDetails(id: Int): DomainResult<CharacterDetailsDomain>
}
