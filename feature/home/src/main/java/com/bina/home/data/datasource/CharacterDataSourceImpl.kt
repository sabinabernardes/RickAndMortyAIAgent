package com.bina.home.data.datasource

import com.bina.home.data.model.CharacterData
import com.bina.home.data.remote.RickAndMortyApiService

class CharacterDataSourceImpl(
    private val apiService: RickAndMortyApiService
) : CharacterDataSource {
    override suspend fun getCharacters(query: String, page: Int): List<CharacterData> {
        val response = apiService.getCharacters(query, page)
        return response.results
    }
}
