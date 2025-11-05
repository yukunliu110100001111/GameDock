package com.example.gamedock.ui.detail

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
fun DetailRoute(
    viewModel: DetailViewModel = viewModel()
) {
    val selectedGame by viewModel.selectedGame.collectAsState()
    DetailScreen(selectedGameTitle = selectedGame?.title ?: "Select a game")
}

@Composable
fun DetailScreen(selectedGameTitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = selectedGameTitle,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
