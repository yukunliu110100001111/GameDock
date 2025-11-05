package com.example.gamedock.domain.model

/**
 * Domain representation of a game that can surface in freebies, watchlist,
 * or deal comparisons.
 */
data class Game(
    val id: String,
    val title: String,
    val store: String,
    val imageUrl: String,
    val price: Double? = null,
    val endTimeMillis: Long? = null,
    val isFree: Boolean = false
)
