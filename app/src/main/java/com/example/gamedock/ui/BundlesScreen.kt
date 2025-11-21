package com.example.gamedock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.ui.platform.LocalUriHandler
import coil.compose.AsyncImage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Surface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import com.example.gamedock.data.util.CurrencyUtils

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
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchNow(searchQuery.value) }),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery.value = ""
                            viewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(Dimens.cardSpacing))

        // 标题 + 结果数 + 排序按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = buildString {
                    append("Bundles")
                    if (!uiState.isLoading && uiState.bundles.isNotEmpty()) {
                        append(" (")
                        append(uiState.bundles.size)
                        append(")")
                    }
                },
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(onClick = { viewModel.toggleSort() }) {
                Icon(Icons.Filled.SwapVert, contentDescription = "Toggle sort")
            }
        }

        // 错误消息
        uiState.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        when {
            uiState.isLoading -> {
                // 简单骨架：显示若干占位卡片
                LazyColumn { items(4) { SkeletonBundleCard() } }
            }
            uiState.bundles.isEmpty() && uiState.query.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.no_results_found),
                            contentDescription = "No results found",
                            modifier = Modifier.size(220.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No bundles found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
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
}

@Composable
private fun SkeletonBundleCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {}
}

@Composable
fun BundleCard(bundle: BundleInfo) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val parsedExpiry = remember(bundle.expiry) { parseExpiry(bundle.expiry) }
    val remaining = remember(parsedExpiry) { remainingLabel(parsedExpiry) }
    val urgent = remaining?.contains("h") == true // 简单判断：剩余显示小时即紧急

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { if (bundle.link.isNotBlank()) uriHandler.openUri(bundle.link) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (urgent) 6.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // crossfade 图片
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(bundle.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = bundle.title,
                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                error = painterResource(id = R.drawable.ic_launcher_foreground),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                // StoreChip 突出商店
                StoreChip(store = bundle.store)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bundle.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.format(bundle.price, bundle.currency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                // 过期剩余徽章
                if (remaining != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(
                        text = remaining + if (urgent) " left" else "",
                        color = if (urgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                } else if (bundle.expiry != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(text = "Expired", color = MaterialTheme.colorScheme.errorContainer)
                }
            }
            if (bundle.link.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { uriHandler.openUri(bundle.link) }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "go to store")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("open")
                }
            }
        }
    }
}

@Composable
private fun StoreChip(store: String) {
    val bg = storeColor(store)
    Surface(
        color = bg.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp
    ) {
        Text(
            text = store,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = bg
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.25f),
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color.darken(0.25f)
        )
    }
}

@Composable
private fun storeColor(store: String): Color = when {
    store.contains("Steam", true) -> Color(0xFF1B6FBC)
    store.contains("Epic", true) -> Color(0xFF9146FF)
    store.contains("GOG", true) -> Color(0xFF673AB7)
    store.contains("Humble", true) -> Color(0xFFE9642E)
    store.contains("Ubisoft", true) -> Color(0xFF2E82D8)
    else -> MaterialTheme.colorScheme.primary
}

private fun Color.darken(factor: Float): Color {
    val r = (red * (1 - factor)).coerceIn(0f, 1f)
    val g = (green * (1 - factor)).coerceIn(0f, 1f)
    val b = (blue * (1 - factor)).coerceIn(0f, 1f)
    return Color(r, g, b, alpha)
}

// 日期解析 & 剩余时间标签
private fun parseExpiry(raw: String?): Date? {
    if (raw.isNullOrBlank()) return null
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val d = sdf.parse(raw)
            if (d != null) return d
        } catch (_: Exception) {}
    }
    return null
}

private fun remainingLabel(expiry: Date?): String? {
    if (expiry == null) return null
    val now = System.currentTimeMillis()
    val diff = expiry.time - now
    if (diff <= 0) return null
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        days >= 1 -> "${days}d"
        hours >= 1 -> "${hours}h"
        else -> "${minutes}m"
    }
}

data class BundlesUiState(
    val isLoading: Boolean = true,
    val bundles: List<BundleInfo> = emptyList(),
    val query: String = "",
    val sortAscending: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class BundlesViewModel @Inject constructor(
    private val repository: DealsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(BundlesUiState(isLoading = false))
    val uiState: StateFlow<BundlesUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query, isLoading = true, errorMessage = null)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchNow(query)
        }
    }

    fun toggleSort() {
        val newAscending = !_uiState.value.sortAscending
        _uiState.value = _uiState.value.copy(sortAscending = newAscending)
        // 重新排序现有列表
        _uiState.value = _uiState.value.copy(
            bundles = sortBundles(_uiState.value.bundles, newAscending)
        )
    }

    private fun sortBundles(list: List<BundleInfo>, ascending: Boolean): List<BundleInfo> =
        if (ascending) list.sortedBy { it.price } else list.sortedByDescending { it.price }

    fun searchNow(query: String = _uiState.value.query) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false, bundles = emptyList())
                return@launch
            }
            try {
                val bundles = repository.searchBundles(query)
                val sorted = sortBundles(bundles, _uiState.value.sortAscending)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bundles = sorted,
                    errorMessage = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    bundles = emptyList(),
                    errorMessage = e.message ?: "Load failed"
                )
            }
        }
    }
}
