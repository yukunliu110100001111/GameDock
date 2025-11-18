package com.example.gamedock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun BundlesScreen(viewModel: BundlesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Bundles",
            style = MaterialTheme.typography.headlineSmall
        )

        if (uiState.isLoading) {
            Text(
                text = "Loading bundle deals…",
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        } else if (uiState.bundles.isEmpty()) {
            Text(
                text = "Bundle deals coming soon.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }
    }
}

data class BundlesUiState(
    val isLoading: Boolean = true,
    val bundles: List<String> = emptyList()
)

@HiltViewModel
class BundlesViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BundlesUiState(isLoading = false))
    val uiState: StateFlow<BundlesUiState> = _uiState.asStateFlow()
}
