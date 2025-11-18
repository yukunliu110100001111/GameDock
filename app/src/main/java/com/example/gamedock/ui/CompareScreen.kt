package com.example.gamedock.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.components.PriceCard
import com.example.gamedock.ui.components.SectionHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: CompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val errorMessage = uiState.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text("Search game...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow() })
        )

        Spacer(modifier = Modifier.height(Dimens.cardSpacing))

        SectionHeader("💰 Price Comparison")

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = Dimens.cardSpacing)
                    .align(Alignment.CenterHorizontally)
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        if (!uiState.isLoading && uiState.results.isEmpty() && uiState.query.isNotBlank() && errorMessage == null) {
            Text(
                text = "No offers found for \"${uiState.query}\".",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.screenPadding)
        ) {
            items(uiState.results) { offer ->
                PriceCard(offer = offer)
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

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val repository: DealsRepository
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
            runCatching { repository.comparePrices(query) }
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

}
