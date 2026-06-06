package com.bina.character_details.data.remote

import com.bina.character_details.data.model.CharacterDetailsData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CharacterDetailsApiService {
    @GET("character/{id}")
    suspend fun getCharacterDetails(
        @Path("id") id: Int
    ): Response<CharacterDetailsData>
}