package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import com.bina.network.NetworkResult

class GetCharacterDetailsUseCase(
    private val repository: CharacterDetailsRepository
) {
    suspend operator fun invoke(id: Int): NetworkResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(id)
}
