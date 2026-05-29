package com.bina.home.data.model

import com.google.gson.annotations.SerializedName

data class CharacterData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("species") val species: String,
    @SerializedName("image") val image: String,
    @SerializedName("location") val location: LocationData
)

data class LocationData(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)