package com.bina.character_details.data.repository

import com.bina.character_details.data.datasource.EpisodeDataSource
import com.bina.character_details.data.mapper.EpisodeMapper
import com.bina.character_details.domain.model.EpisodeDomain
import com.bina.character_details.domain.repository.EpisodeRepository
import com.bina.network.NetworkResult
import com.bina.network.safeApiCall

class EpisodeRepositoryImpl(
    private val dataSource: EpisodeDataSource
) : EpisodeRepository {
    override suspend fun getEpisodes(ids: List<Int>): NetworkResult<List<EpisodeDomain>> =
        safeApiCall { dataSource.getEpisodes(ids).map(EpisodeMapper::toDomain) }
}
