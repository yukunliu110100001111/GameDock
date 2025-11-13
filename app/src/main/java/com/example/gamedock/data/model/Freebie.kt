package com.example.gamedock.data.model

data class Freebie(
    val id: String,
    val title: String,
    val store: String,
    val claimUrl: String?,
    val startDate: String? = null,
    val endDate: String? = null,
    val isClaimed: Boolean = false
)
