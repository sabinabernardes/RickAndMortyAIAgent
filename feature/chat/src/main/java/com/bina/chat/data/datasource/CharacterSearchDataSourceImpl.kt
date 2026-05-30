package com.bina.chat.data.datasource

import com.bina.chat.data.remote.CharacterSearchApiService

class CharacterSearchDataSourceImpl(
    private val api: CharacterSearchApiService
) : CharacterSearchDataSource {

    override suspend fun searchByName(name: String): Int? =
        runCatching { api.searchByName(name).results?.firstOrNull()?.id }.getOrNull()
}
