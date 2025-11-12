package com.example.gamedock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Welcome to GameDock!",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Explore the latest freebies, compare prices, and manage your watchlist.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Dimens.cardSpacing)
        )
    }
}
