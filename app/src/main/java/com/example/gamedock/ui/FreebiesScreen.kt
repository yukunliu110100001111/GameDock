package com.example.gamedock.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Game
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.components.GameCard
import com.example.gamedock.ui.components.SectionHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun FreebiesScreen(
    repository: DealsRepository,
    viewModel: FreebiesViewModel = viewModel(factory = FreebiesViewModel.factory(repository))
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
                    GameCard(game = game, onClick = { /* TODO: details */ })
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (!uiState.isLoading && uiState.games.isEmpty() && uiState.errorMessage == null) {
            Text(
                text = "No freebies found. Pull to refresh to try again.",
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
            TextButton(onClick = viewModel::refresh) {
                Text("Refresh")
            }
        }
    }
}

private data class FreebiesUiState(
    val isLoading: Boolean = false,
    val games: List<Game> = emptyList(),
    val errorMessage: String? = null
)

class FreebiesViewModel(
    private val repository: DealsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreebiesUiState(isLoading = true))
    val uiState: StateFlow<FreebiesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { repository.getFreebies() }
                .onSuccess { games ->
                    _uiState.value = FreebiesUiState(games = games)
                }
                .onFailure { throwable ->
                    _uiState.value = FreebiesUiState(
                        errorMessage = throwable.message ?: "Unable to load freebies"
                    )
                }
        }
    }

    companion object {
        fun factory(repository: DealsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(FreebiesViewModel::class.java)) {
                        return FreebiesViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
                }
            }
    }
}
