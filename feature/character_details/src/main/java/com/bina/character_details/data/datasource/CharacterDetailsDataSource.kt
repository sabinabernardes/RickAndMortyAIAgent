package com.bina.character_details.data.datasource

import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.data.remote.CharacterDetailsApiService
import retrofit2.Response

interface CharacterDetailsDataSource {
    suspend fun getCharacterDetails(id: Int): Response<CharacterDetailsData>
}

class CharacterDetailsDataSourceImpl(
    private val apiService: CharacterDetailsApiService
) : CharacterDetailsDataSource {
    override suspend fun getCharacterDetails(id: Int): Response<CharacterDetailsData> =
        apiService.getCharacterDetails(id)
}
