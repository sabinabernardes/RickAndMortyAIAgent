package com.bina.character_details.data.datasource

import com.bina.character_details.data.model.CharacterDetailsData
import com.bina.character_details.data.remote.CharacterDetailsApiService

interface CharacterDetailsDataSource {
    suspend fun getCharacterDetails(id: Int): CharacterDetailsData
}

class CharacterDetailsDataSourceImpl(
    private val apiService: CharacterDetailsApiService
) : CharacterDetailsDataSource {
    override suspend fun getCharacterDetails(id: Int): CharacterDetailsData {
        return apiService.getCharacterDetails(id)
    }
}
