package com.example.gamedock.data.remote.dto

/**
 * DTO for price comparison responses.
 */
data class OfferDto(
    val id: String,
    val gameTitle: String,
    val store: String,
    val currentPrice: Double,
    val lowestPrice: Double,
    val currencyCode: String,
    val url: String?
)
