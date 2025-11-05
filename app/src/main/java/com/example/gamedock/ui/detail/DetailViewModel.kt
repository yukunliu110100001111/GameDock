package com.example.gamedock.ui.detail

import androidx.lifecycle.ViewModel
import com.example.gamedock.domain.model.Game
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Placeholder detail ViewModel that will eventually load game-specific info.
 */
class DetailViewModel : ViewModel() {
    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame.asStateFlow()

    fun loadGame(game: Game) {
        _selectedGame.value = game
    }
}
