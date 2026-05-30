package com.bina.chat.search.data.datasource

interface CharacterSearchDataSource {
    suspend fun searchByName(name: String): Int?
}
