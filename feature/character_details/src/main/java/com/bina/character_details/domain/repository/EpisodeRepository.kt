package com.bina.character_details.domain.repository

import com.bina.character_details.domain.model.EpisodeDomain

interface EpisodeRepository {
    suspend fun getEpisodes(ids: List<Int>): List<EpisodeDomain>
}
