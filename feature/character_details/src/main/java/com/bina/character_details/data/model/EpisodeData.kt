package com.bina.character_details.data.model

import com.google.gson.annotations.SerializedName

data class EpisodeData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("episode") val episode: String,
    @SerializedName("air_date") val airDate: String
)
