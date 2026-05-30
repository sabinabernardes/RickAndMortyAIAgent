package com.bina.character_details.data.datasource

import com.bina.character_details.data.model.EpisodeData
import com.bina.character_details.data.remote.EpisodeApiService

interface EpisodeDataSource {
    suspend fun getEpisodes(ids: List<Int>): List<EpisodeData>
}

class EpisodeDataSourceImpl(
    private val apiService: EpisodeApiService
) : EpisodeDataSource {
    override suspend fun getEpisodes(ids: List<Int>): List<EpisodeData> {
        // API returns a single object (not array) when only one ID is requested — ADR-010
        return if (ids.size == 1) {
            listOf(apiService.getEpisodeSingle(ids.first()))
        } else {
            apiService.getEpisodes(ids.joinToString(","))
        }
    }
}
