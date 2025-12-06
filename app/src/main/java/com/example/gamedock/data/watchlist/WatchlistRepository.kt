package com.example.gamedock.data.watchlist

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {

    fun watchlistFlow(): Flow<List<WatchlistEntity>>
    suspend fun addOrUpdate(item: WatchlistEntity)
    suspend fun remove(gameId: String)

    suspend fun get(gameId: String): WatchlistEntity?

    suspend fun updateLastKnownPrice(gameId: String, newPrice: Double)
    suspend fun setNotificationsEnabled(gameId: String, enabled: Boolean)
}
