package com.example.gamedock.domain.model

/**
 * Information about bundles that package multiple offers together.
 */
data class BundleInfo(
    val id: String,
    val title: String,
    val games: List<Game>,
    val price: Double,
    val currencyCode: String = "USD",
    val expiresAtMillis: Long? = null
)
