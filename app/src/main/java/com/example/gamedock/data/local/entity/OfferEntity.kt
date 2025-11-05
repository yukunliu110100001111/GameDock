package com.example.gamedock.data.local.entity

/**
 * Local Room entity placeholder for offers.
 */
data class OfferEntity(
    val id: String,
    val gameTitle: String,
    val store: String,
    val currentPrice: Double,
    val lowestPrice: Double
)
