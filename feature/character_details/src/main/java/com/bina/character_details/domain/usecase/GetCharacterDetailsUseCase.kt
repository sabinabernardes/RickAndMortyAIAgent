package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.CharacterDetailsDomain
import com.bina.character_details.domain.repository.CharacterDetailsRepository
import com.bina.domain.DomainResult
import com.bina.domain.NoOpUseCaseObserver
import com.bina.domain.ObservableUseCase
import com.bina.domain.UseCaseObserver

class GetCharacterDetailsUseCase(
    private val repository: CharacterDetailsRepository,
    observer: UseCaseObserver = NoOpUseCaseObserver
) : ObservableUseCase<Int, CharacterDetailsDomain>(observer) {

    override suspend fun execute(params: Int): DomainResult<CharacterDetailsDomain> =
        repository.getCharacterDetails(params)
}
