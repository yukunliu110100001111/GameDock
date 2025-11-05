package com.example.gamedock.domain.model

/**
 * Represents a single historical price point for a game at a given store.
 */
data class PricePoint(
    val store: String,
    val current: Double,
    val lowest: Double
)
