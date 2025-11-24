package com.example.gamedock.data.repository

import com.example.gamedock.data.local.watchlist.WatchlistEntity
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun watchlistFlow(): Flow<List<WatchlistEntity>>
    suspend fun add(item: WatchlistEntity)
    suspend fun remove(gameId: String)
    suspend fun get(gameId: String): WatchlistEntity?
    suspend fun updateLastKnownPrice(gameId: String, newPrice: Double)
}
