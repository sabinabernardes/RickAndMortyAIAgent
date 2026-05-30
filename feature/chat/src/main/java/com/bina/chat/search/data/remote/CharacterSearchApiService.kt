package com.bina.chat.search.data.remote

import com.bina.chat.search.data.model.CharacterSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CharacterSearchApiService {
    @GET("character")
    suspend fun searchByName(@Query("name") name: String): CharacterSearchResponse
}
