package com.bina.character_details.data.datasource

import com.bina.character_details.data.model.EpisodeData
import com.bina.character_details.data.remote.EpisodeApiService
import retrofit2.HttpException
import retrofit2.Response

interface EpisodeDataSource {
    suspend fun getEpisodes(ids: List<Int>): List<EpisodeData>
}

class EpisodeDataSourceImpl(
    private val apiService: EpisodeApiService
) : EpisodeDataSource {
    // ADR-010: a API retorna objeto único (não array) quando há apenas um ID
    override suspend fun getEpisodes(ids: List<Int>): List<EpisodeData> {
        return if (ids.size == 1) {
            apiService.getEpisodeSingle(ids.first()).bodyOrThrow().let(::listOf)
        } else {
            apiService.getEpisodes(ids.joinToString(",")).bodyOrThrow()
        }
    }

    private fun <T> Response<T>.bodyOrThrow(): T {
        if (isSuccessful) {
            return body() ?: throw HttpException(this)
        } else {
            throw HttpException(this)
        }
    }
}
