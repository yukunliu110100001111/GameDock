package com.example.gamedock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.components.PriceCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.gamedock.R
import com.example.gamedock.data.util.CurrencyUtils

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
        // 搜索框：清空按钮 + 加载指示
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onSearchQueryChange,
            label = { Text("Search game...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow() }),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimens.cardSpacing))

        // Header 行：标题 + 结果数 + 排序按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val countText = if (!uiState.isLoading && uiState.results.isNotEmpty()) " (" + uiState.results.size + ")" else ""
            Text(
                text = "💰 Price Comparison$countText",
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(onClick = { viewModel.toggleSort() }) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Toggle sort")
            }
        }

        // 最优报价摘要
        BestOfferSummary(uiState.results)

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(Dimens.cardSpacing))
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Dimens.cardSpacing)
            )
        }

        // 无结果且有搜索词且无错误
        if (!uiState.isLoading && uiState.results.isEmpty() && uiState.query.isNotBlank() && errorMessage == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.no_results_found),
                        contentDescription = "No offers",
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No offers found for \"${uiState.query}\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = Dimens.screenPadding)
        ) {
            // 骨架
            if (uiState.isLoading) {
                items(4) { SkeletonPriceCard() }
            }
            // 真实数据
            itemsIndexed(uiState.results) { index, offer ->
                val isBest = uiState.resultsMinPrice() == offer.currentPrice
                Box(Modifier.fillMaxWidth()) {
                    PriceCard(offer = offer)
                    if (isBest) {
                        BestBadge(Modifier.align(Alignment.TopEnd))
                    }
                }
            }
        }
    }
}

@Composable
private fun BestOfferSummary(results: List<Offer>) {
    if (results.isEmpty()) return
    val min = results.minByOrNull { it.currentPrice } ?: return
    val max = results.maxByOrNull { it.currentPrice } ?: min
    val diff = max.currentPrice - min.currentPrice
    val diffPercent = if (max.currentPrice > 0) (diff / max.currentPrice * 100) else 0.0
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Best: ${min.store} ${CurrencyUtils.format(min.currentPrice, min.currencyCode)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (results.size > 1 && diff > 0) {
                Text(
                    text = "Vs Highest: save ${CurrencyUtils.format(diff, min.currencyCode)} (~${"%.0f".format(diffPercent)}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BestBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(end = 8.dp, top = 4.dp)
            .clip(RoundedCornerShape(50)),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 0.dp
    ) {
        Text(
            text = "BEST",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SkeletonPriceCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(100.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {}
}

// 扩展函数：获取最小价（用于标记最佳）
private fun CompareUiState.resultsMinPrice(): Double? = results.minByOrNull { it.currentPrice }?.currentPrice

data class CompareUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Offer> = emptyList(),
    val errorMessage: String? = null,
    val sortAscending: Boolean = true
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

    fun toggleSort() {
        val newAsc = !_uiState.value.sortAscending
        _uiState.value = _uiState.value.copy(
            sortAscending = newAsc,
            results = sortOffers(_uiState.value.results, newAsc)
        )
    }

    private fun sortOffers(list: List<Offer>, ascending: Boolean): List<Offer> =
        if (ascending) list.sortedBy { it.currentPrice } else list.sortedByDescending { it.currentPrice }

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
                        results = sortOffers(offers, _uiState.value.sortAscending)
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
