package com.bina.character_details.data.model

import com.google.gson.annotations.SerializedName

data class CharacterDetailsData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("species") val species: String,
    @SerializedName("type") val type: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("origin") val origin: OriginData,
    @SerializedName("location") val location: LocationDetailsData,
    @SerializedName("image") val image: String,
    @SerializedName("episode") val episode: List<String>,
    @SerializedName("url") val url: String,
    @SerializedName("created") val created: String
)

data class OriginData(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class LocationDetailsData(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)
