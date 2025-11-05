package com.example.gamedock.domain.usecase

import com.example.gamedock.domain.model.WatchItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Placeholder watchlist use case to be expanded once persistence is available.
 */
class ManageWatchlistUseCase {
    private val watchlistState = MutableStateFlow<List<WatchItem>>(emptyList())

    fun watchlist(): Flow<List<WatchItem>> = watchlistState

    fun add(item: WatchItem) {
        watchlistState.value = watchlistState.value + item
    }

    fun remove(id: String) {
        watchlistState.value = watchlistState.value.filterNot { it.id == id }
    }
}
