package com.example.gamedock.data.model

/**
 * Represents a charity/discount bundle with its included games.
 */
data class BundleDeal(
    val id: Int,
    val title: String,
    val store: String,
    val price: Double,
    val currency: String,
    val expiry: String?,
    val link: String,
    val imageUrl: String?,
    val games: List<BundleGame>
)

data class BundleGame(
    val title: String,
    val imageUrl: String?
)
