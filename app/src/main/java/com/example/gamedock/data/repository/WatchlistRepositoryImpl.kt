package com.example.gamedock.data.repository

import com.example.gamedock.data.local.watchlist.WatchlistDao
import com.example.gamedock.data.local.watchlist.WatchlistEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val dao: WatchlistDao
) : WatchlistRepository {

    override fun watchlistFlow(): Flow<List<WatchlistEntity>> = dao.getAll()

    override suspend fun add(item: WatchlistEntity) {
        dao.insert(item)
    }

    override suspend fun remove(gameId: String) {
        dao.deleteById(gameId)
    }

    override suspend fun get(gameId: String): WatchlistEntity? {
        return dao.getById(gameId)
    }

    override suspend fun updateLastKnownPrice(gameId: String, newPrice: Double) {
        val item = dao.getById(gameId) ?: return
        dao.update(item.copy(lastKnownPrice = newPrice))
    }
}
