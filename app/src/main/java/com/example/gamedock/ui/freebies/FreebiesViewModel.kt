package com.example.gamedock.ui.freebies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.domain.model.Game
import com.example.gamedock.domain.usecase.GetFreebiesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Handles loading freebies from the repository and exposes a simple UI state.
 */
class FreebiesViewModel(
    private val getFreebiesUseCase: GetFreebiesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreebiesUiState())
    val uiState: StateFlow<FreebiesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { getFreebiesUseCase() }
                .onSuccess { games -> _uiState.value = FreebiesUiState(games = games) }
                .onFailure { throwable ->
                    _uiState.value = FreebiesUiState(
                        errorMessage = throwable.message ?: "Unable to load freebies"
                    )
                }
        }
    }

    companion object {
        fun provideFactory(repository: DealsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(FreebiesViewModel::class.java)) {
                        return FreebiesViewModel(GetFreebiesUseCase(repository)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                }
            }
    }
}

data class FreebiesUiState(
    val isLoading: Boolean = false,
    val games: List<Game> = emptyList(),
    val errorMessage: String? = null
)
