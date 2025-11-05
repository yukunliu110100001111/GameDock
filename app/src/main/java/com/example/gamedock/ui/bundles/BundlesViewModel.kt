package com.example.gamedock.ui.bundles

import androidx.lifecycle.ViewModel
import com.example.gamedock.domain.model.BundleInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Placeholder ViewModel for bundles, returns mock data until repository wiring arrives.
 */
class BundlesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BundlesUiState())
    val uiState: StateFlow<BundlesUiState> = _uiState.asStateFlow()

    init {
        // TODO: integrate repository when available
        _uiState.value = BundlesUiState(isLoading = false, bundles = emptyList())
    }
}

data class BundlesUiState(
    val isLoading: Boolean = true,
    val bundles: List<BundleInfo> = emptyList()
)
