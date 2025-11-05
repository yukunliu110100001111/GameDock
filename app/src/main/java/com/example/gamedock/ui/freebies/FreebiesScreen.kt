package com.example.gamedock.ui.freebies

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamedock.core.design.Dimens
import com.example.gamedock.core.design.Strings
import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.ui.components.GameCard
import com.example.gamedock.ui.components.SectionHeader

@Composable
fun FreebiesRoute(
    repository: DealsRepository,
    viewModel: FreebiesViewModel = viewModel(factory = FreebiesViewModel.provideFactory(repository))
    // nav actions go here when detail screen wired up
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = "Retry"
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.refresh()
        }
    }

    FreebiesScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh
    )
}

@Composable
fun FreebiesScreen(
    uiState: FreebiesUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        SectionHeader("🎁 ${Strings.freebiesTitle}")

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Dimens.screenPadding)
            ) {
                items(uiState.games) { game ->
                    GameCard(game = game, onClick = { /* TODO: navigate to details */ })
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (!uiState.isLoading && uiState.games.isEmpty() && uiState.errorMessage == null) {
            Text(
                text = "No freebies found. Pull to refresh to try again.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
            TextButton(onClick = onRefresh) {
                Text(text = "Refresh")
            }
        }
    }
}
