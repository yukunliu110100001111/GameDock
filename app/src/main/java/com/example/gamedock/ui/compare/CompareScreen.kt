package com.example.gamedock.ui.compare

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.gamedock.core.design.Dimens
import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.ui.components.PriceCard
import com.example.gamedock.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareRoute(
    repository: DealsRepository,
    viewModel: CompareViewModel = viewModel(factory = CompareViewModel.provideFactory(repository))
) {
    val uiState by viewModel.uiState.collectAsState()

    CompareScreen(
        uiState = uiState,
        onQueryChange = viewModel::onSearchQueryChange,
        onSearch = viewModel::searchNow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    uiState: CompareUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text("Search game...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            )
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

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        if (!uiState.isLoading && uiState.results.isEmpty() && uiState.query.isNotBlank() && uiState.errorMessage == null) {
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
