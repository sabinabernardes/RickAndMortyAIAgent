package com.bina.home.data.remote

import com.bina.home.data.model.CharacterResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RickAndMortyApiService {
    @GET("character")
    suspend fun getCharacters(
        @Query("name") name: String?,
        @Query("page") page: Int
    ): CharacterResponse
}
