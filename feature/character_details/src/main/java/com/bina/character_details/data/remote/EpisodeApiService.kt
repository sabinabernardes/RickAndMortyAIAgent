package com.bina.character_details.data.remote

import com.bina.character_details.data.model.EpisodeData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface EpisodeApiService {
    // Returns a single object when called with one ID — use getEpisodeSingle for that case
    @GET("episode/{ids}")
    suspend fun getEpisodes(@Path("ids") ids: String): Response<List<EpisodeData>>

    @GET("episode/{id}")
    suspend fun getEpisodeSingle(@Path("id") id: Int): Response<EpisodeData>
}