package com.bina.character_details.domain.usecase

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.repository.EpisodeRepository

class GetEpisodesUseCase(private val repository: EpisodeRepository) {
    suspend operator fun invoke(ids: List<Int>): List<EpisodeDomain> = repository.getEpisodes(ids)
}
