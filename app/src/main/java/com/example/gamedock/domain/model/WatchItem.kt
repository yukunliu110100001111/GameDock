package com.example.gamedock.domain.model

/**
 * Entry persisted in the watchlist, combining a game with target pricing info.
 */
data class WatchItem(
    val id: String,
    val game: Game,
    val targetPrice: Double?,
    val notificationsEnabled: Boolean
)
