package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository

class GetCharacterDetailsUseCase(
    private val repository: CharacterDetailsRepository
) {
    suspend operator fun invoke(id: Int): CharacterDetailsDomain {
        return repository.getCharacterDetails(id)
    }
}
