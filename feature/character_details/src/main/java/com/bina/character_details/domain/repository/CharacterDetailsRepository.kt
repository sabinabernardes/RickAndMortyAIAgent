package com.bina.character_details.domain.repository

import com.bina.character_details.domain.model.CharacterDetailsDomain

interface CharacterDetailsRepository {
    suspend fun getCharacterDetails(id: Int): CharacterDetailsDomain
}
