package com.bina.character_details.domain.repository

import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.network.NetworkResult

interface EpisodeRepository {
    suspend fun getEpisodes(ids: List<Int>): NetworkResult<List<EpisodeDomain>>
}
