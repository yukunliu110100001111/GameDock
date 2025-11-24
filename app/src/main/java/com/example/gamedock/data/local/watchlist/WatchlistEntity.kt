package com.example.gamedock.data.local.watchlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity (
    @PrimaryKey val gameId: String,
    val title: String,
    val imageUrl: String? = null,
    val url: String? = null,
    val lastKnownPrice: Double = Double.MAX_VALUE,
    val currency: String = "USD",
    val addedTime: Long = System.currentTimeMillis()
)