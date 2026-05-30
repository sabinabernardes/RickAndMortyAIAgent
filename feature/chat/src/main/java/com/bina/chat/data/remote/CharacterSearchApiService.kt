package com.bina.chat.data.remote

import com.bina.chat.data.model.CharacterSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CharacterSearchApiService {
    @GET("character")
    suspend fun searchByName(@Query("name") name: String): CharacterSearchResponse
}
