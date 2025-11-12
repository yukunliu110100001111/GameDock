package com.example.gamedock.data.model

/**
 * Represents a single historical price entry for a game.
 */
data class PricePoint(
    val store: String,
    val current: Double,
    val lowest: Double
)
