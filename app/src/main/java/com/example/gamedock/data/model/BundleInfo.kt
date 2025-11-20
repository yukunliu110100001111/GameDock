package com.example.gamedock.data.model

/**
 * Represents a bundle of multiple games sold together.
 */
data class BundleInfo(
    val title: String,
    val store: String,
    val price: Double,
    val currency: String,
    val expiry: String?,
    val link: String,
    val imageUrl: String?
)
