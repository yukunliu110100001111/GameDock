package com.example.gamedock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewModelScope
import com.example.gamedock.R
import com.example.gamedock.data.model.BundleInfo
import com.example.gamedock.data.repository.DealsRepository

@Composable
fun BundlesScreen(
    viewModel: BundlesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        OutlinedTextField(
            value = searchQuery.value,
            onValueChange = { query ->
                searchQuery.value = query
                viewModel.onSearchQueryChange(query)
            },
            label = { Text("Search bundles...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow() })
        )

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
                text = "No bundles found.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.bundles) { bundle ->
                    BundleCard(bundle)
                }
            }
        }
    }
}

@Composable
fun BundleCard(bundle: BundleInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bundle.imageUrl?.let {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .width(64.dp)
                    .height(64.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = bundle.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${bundle.store} - ${bundle.price} ${bundle.currency}",
                style = MaterialTheme.typography.bodyMedium
            )
            bundle.expiry?.let {
                Text(
                    text = "Expires: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

data class BundlesUiState(
    val isLoading: Boolean = true,
    val bundles: List<BundleInfo> = emptyList(), // 修改类型为 List<BundleInfo>
    val query: String = "" // Added query field to hold the search query
)

@HiltViewModel
class BundlesViewModel @Inject constructor(
    private val repository: DealsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BundlesUiState(isLoading = false))
    val uiState: StateFlow<BundlesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchNow(query)
        }
    }

    fun searchNow(query: String = _uiState.value.query) {
        viewModelScope.launch {
            try {
                val filteredBundles = repository.searchBundles(query)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bundles = filteredBundles
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bundles = emptyList()
                )
            }
        }
    }
}
