package com.bina.character_details.domain.model

data class CharacterDetailsDomain(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String,
    val location: String,
    val image: String,
    val episodeUrls: List<String>
)
