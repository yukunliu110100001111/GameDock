package com.example.gamedock.data.local.entity

data class WatchlistEntity(
    val gameId: String,
    val title: String,
    val imageUrl: String? = null,
    val url: String? = null,
    val lastKnownPrice: Double = Double.MAX_VALUE,
    val currency: String = "USD",
    val preferredStores: List<String> = emptyList(),
    val addedTime: Long = System.currentTimeMillis()
)
