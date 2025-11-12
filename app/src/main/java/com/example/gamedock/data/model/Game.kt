package com.example.gamedock.data.model

/**
 * Core representation of a game used throughout the app.
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
