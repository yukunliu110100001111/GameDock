package com.example.gamedock.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.domain.model.WatchItem
import com.example.gamedock.domain.usecase.ManageWatchlistUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Placeholder ViewModel for watchlist management until persistence is wired up.
 */
class WatchlistViewModel(
    private val manageWatchlistUseCase: ManageWatchlistUseCase = ManageWatchlistUseCase()
) : ViewModel() {

    val uiState: StateFlow<WatchlistUiState> = manageWatchlistUseCase.watchlist()
        .map { WatchlistUiState(items = it, isEmpty = it.isEmpty()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WatchlistUiState(isEmpty = true)
        )

    fun removeItem(id: String) {
        manageWatchlistUseCase.remove(id)
    }
}

data class WatchlistUiState(
    val items: List<WatchItem> = emptyList(),
    val isEmpty: Boolean = false
)
