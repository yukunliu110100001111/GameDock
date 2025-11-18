package com.example.gamedock.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GamerPowerDto(
    val id: Int,
    val title: String,
    val worth: String?,
    val thumbnail: String?,
    val image: String?,
    val description: String?,
    val instructions: String?,
    @SerializedName("open_giveaway_url")
    val openGiveawayUrl: String?,
    @SerializedName("published_date")
    val publishedDate: String?,
    val type: String?,
    val platforms: String?,
    @SerializedName("end_date")
    val endDate: String?,
    val users: Int?,
    val status: String?,
    @SerializedName("gamerpower_url")
    val gamerPowerUrl: String?
)
