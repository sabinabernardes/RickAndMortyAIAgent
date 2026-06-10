package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.repository.EpisodeRepository
import com.bina.domain.DomainResult
import com.bina.domain.NoOpUseCaseObserver
import com.bina.domain.ObservableUseCase
import com.bina.domain.UseCaseObserver

class GetEpisodesUseCase(
    private val repository: EpisodeRepository,
    observer: UseCaseObserver = NoOpUseCaseObserver
) : ObservableUseCase<List<Int>, List<EpisodeDomain>>(observer) {

    override suspend fun execute(params: List<Int>): DomainResult<List<EpisodeDomain>> =
        repository.getEpisodes(params)
}
