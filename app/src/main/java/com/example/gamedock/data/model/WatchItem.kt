package com.example.gamedock.data.model

/**
 * Entry stored in a user's watchlist along with alert preferences.
 */
data class WatchItem(
    val id: String,
    val game: Game,
    val targetPrice: Double?,
    val notificationsEnabled: Boolean
)
