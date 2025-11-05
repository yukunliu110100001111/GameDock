package com.example.gamedock.ui.watchlist

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedock.core.design.Dimens

@Composable
fun WatchlistRoute(
    viewModel: WatchlistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    WatchlistScreen(uiState = uiState, onClear = { /* TODO: open add dialog */ })
}

@Composable
fun WatchlistScreen(
    uiState: WatchlistUiState,
    onClear: () -> Unit
) {
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
                text = "Tracked titles: ${uiState.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        Button(
            onClick = onClear,
            modifier = Modifier.padding(top = Dimens.cardSpacing)
        ) {
            Text(text = "Manage watchlist")
        }
    }
}
