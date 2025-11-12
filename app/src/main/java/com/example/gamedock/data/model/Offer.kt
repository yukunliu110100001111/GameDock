package com.example.gamedock.data.model

/**
 * Represents a price offer for a specific game and store.
 */
data class Offer(
    val id: String,
    val gameTitle: String,
    val store: String,
    val currentPrice: Double,
    val lowestPrice: Double,
    val currencyCode: String = "USD",
    val url: String? = null
)
