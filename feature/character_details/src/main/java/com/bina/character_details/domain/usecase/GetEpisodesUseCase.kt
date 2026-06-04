package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.repository.EpisodeRepository
import com.bina.network.NetworkResult

class GetEpisodesUseCase(private val repository: EpisodeRepository) {
    suspend operator fun invoke(ids: List<Int>): NetworkResult<List<EpisodeDomain>> =
        repository.getEpisodes(ids)
}
