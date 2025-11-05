package com.example.gamedock.data.local.entity

/**
 * Local Room entity placeholder for watchlist entries.
 */
data class WatchItemEntity(
    val id: String,
    val gameId: String,
    val targetPrice: Double?,
    val notificationsEnabled: Boolean
)
