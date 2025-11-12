package com.example.gamedock.data.model

/**
 * Represents a bundle of multiple games sold together.
 */
data class BundleInfo(
    val id: String,
    val title: String,
    val games: List<Game>,
    val price: Double,
    val currencyCode: String = "USD",
    val expiresAtMillis: Long? = null
)
