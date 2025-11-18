package com.example.gamedock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
fun WatchlistScreen(viewModel: WatchlistViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Watchlist",
            style = MaterialTheme.typography.headlineSmall
        )

        if (uiState.isEmpty) {
            Text(
                text = "Your watchlist is empty. Track a game to get started.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        } else {
            Text(
                text = "Tracked titles: ${uiState.trackedCount}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.padding(top = Dimens.cardSpacing)
        ) {
            Text(text = "Manage watchlist")
        }
    }
}

data class WatchlistUiState(
    val trackedCount: Int = 0,
    val isEmpty: Boolean = true
)

@HiltViewModel
class WatchlistViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()
}
