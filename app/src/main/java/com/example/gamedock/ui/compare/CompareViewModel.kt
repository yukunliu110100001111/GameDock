package com.example.gamedock.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.domain.model.Offer
import com.example.gamedock.domain.usecase.ComparePricesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages search state for the price comparison feature.
 */
class CompareViewModel(
    private val comparePricesUseCase: ComparePricesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        scheduleSearch(query)
    }

    fun searchNow() {
        scheduleSearch(_uiState.value.query, immediate = true)
    }

    private fun scheduleSearch(query: String, immediate: Boolean = false) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (!immediate) delay(300)
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(results = emptyList(), isLoading = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { comparePricesUseCase(query) }
                .onSuccess { offers ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = offers
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to compare prices"
                    )
                }
        }
    }

    companion object {
        fun provideFactory(repository: DealsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(CompareViewModel::class.java)) {
                        return CompareViewModel(ComparePricesUseCase(repository)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                }
            }
    }
}

data class CompareUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Offer> = emptyList(),
    val errorMessage: String? = null
)
