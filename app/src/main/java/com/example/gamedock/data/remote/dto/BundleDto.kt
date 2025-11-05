package com.example.gamedock.data.remote.dto

/**
 * DTO for bundle endpoints.
 */
data class BundleDto(
    val id: String,
    val title: String,
    val gameIds: List<String>,
    val price: Double,
    val currencyCode: String,
    val expiresAtMillis: Long?
)
