package com.bina.character_details.presentation.model

data class CharacterDetailsUiModel(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val origin: String,
    val location: String,
    val imageUrl: String
)
