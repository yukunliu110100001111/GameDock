package com.example.gamedock.ui.bundles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedock.core.design.Dimens

@Composable
fun BundlesRoute(
    viewModel: BundlesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    BundlesScreen(uiState = uiState)
}

@Composable
fun BundlesScreen(uiState: BundlesUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Bundles",
            style = MaterialTheme.typography.headlineSmall
        )
        if (uiState.bundles.isEmpty()) {
            Text(
                text = "Bundle deals coming soon.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }
    }
}
