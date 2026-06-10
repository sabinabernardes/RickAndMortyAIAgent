package com.bina.character_details.domain.repository

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.domain.DomainResult

interface EpisodeRepository {
    suspend fun getEpisodes(ids: List<Int>): DomainResult<List<EpisodeDomain>>
}
