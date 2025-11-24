package com.example.gamedock.data.repository

import android.content.Context
import com.example.gamedock.data.local.entity.WatchlistEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) : WatchlistRepository {

    private val prefs = context.getSharedPreferences("watchlist_prefs", Context.MODE_PRIVATE)
    private val key = "watchlist_items"
    private val type = object : TypeToken<List<WatchlistEntity>>() {}.type

    private val _state = MutableStateFlow(loadFromPrefs())
    private fun loadFromPrefs(): List<WatchlistEntity> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<WatchlistEntity>>(json, type) }.getOrElse { emptyList() }
    }

    private fun persist(list: List<WatchlistEntity>) {
        val json = gson.toJson(list, type)
        prefs.edit().putString(key, json).apply()
    }

    override fun watchlistFlow(): Flow<List<WatchlistEntity>> = _state.asStateFlow()

    override suspend fun addOrUpdate(item: WatchlistEntity) {
        val current = _state.value.toMutableList()
        val index = current.indexOfFirst { it.gameId == item.gameId }
        if (index >= 0) {
            // 保留原 addedTime
            val old = current[index]
            current[index] = item.copy(addedTime = old.addedTime)
        } else {
            current.add(item.copy(addedTime = System.currentTimeMillis()))
        }
        _state.value = current
        persist(current)
    }

    override suspend fun remove(gameId: String) {
        val current = _state.value.toMutableList()
        val removed = current.removeAll { it.gameId == gameId }
        if (removed) {
            _state.value = current
            persist(current)
        }
    }

    override suspend fun updateLastKnownPrice(gameId: String, price: Double) {
        val current = _state.value.toMutableList()
        val index = current.indexOfFirst { it.gameId == gameId }
        if (index >= 0) {
            current[index] = current[index].copy(lastKnownPrice = price)
            _state.value = current
            persist(current)
        }
    }

    override suspend fun get(gameId: String): WatchlistEntity? {
        return _state.value.firstOrNull { it.gameId == gameId }
    }

}
