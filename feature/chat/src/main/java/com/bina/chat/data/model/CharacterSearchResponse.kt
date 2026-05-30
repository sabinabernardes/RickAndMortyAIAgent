package com.bina.chat.data.model

data class CharacterSearchResponse(
    val results: List<CharacterSearchResult>?
)

data class CharacterSearchResult(
    val id: Int,
    val name: String
)
