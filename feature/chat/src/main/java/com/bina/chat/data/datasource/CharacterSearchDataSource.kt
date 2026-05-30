package com.bina.chat.data.datasource

interface CharacterSearchDataSource {
    suspend fun searchByName(name: String): Int?
}
