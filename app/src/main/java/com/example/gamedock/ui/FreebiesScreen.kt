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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Game
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.components.GameCard
import com.example.gamedock.ui.components.SectionHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage

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
                items(uiState.freebies) { freebie ->
                    FreebieCard(freebie = freebie)
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

        if (!uiState.isLoading && uiState.freebies.isEmpty() && uiState.errorMessage == null) {
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

@Composable
fun FreebieCard(
    freebie: Freebie,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            // 左侧封面图（如果之后你加 imageUrl，这里自动显示）
            if (freebie.imageUrl != null) {
                AsyncImage(
                    model = freebie.imageUrl,
                    contentDescription = freebie.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // 占位图
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 标题
                Text(
                    text = freebie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 商店名
                Text(
                    text = freebie.store,
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 剩余时间
                Text(
                    text = simpleRemaining(freebie.endDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            freebie.claimUrl?.let { url ->
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(url)
                                )
                                context.startActivity(intent)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Claim")
                    }
                }
            }
        }
    }
}



data class FreebiesUiState(
    val isLoading: Boolean = false,
    val freebies: List<Freebie> = emptyList(),
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
                .onSuccess { freebies ->
                    _uiState.value = FreebiesUiState(freebies = freebies)
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

fun simpleRemaining(endDate: String?): String {
    if (endDate.isNullOrBlank()) return "Ends: Unknown"

    try {
        val year = endDate.substring(0, 4).toInt()
        val month = endDate.substring(5, 7).toInt()
        val day = endDate.substring(8, 10).toInt()
        val hour = endDate.substring(11, 13).toInt()
        val minute = endDate.substring(14, 16).toInt()
        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        val nowMillis = cal.timeInMillis

        cal.set(year, month - 1, day, hour, minute, 0)
        val endMillis = cal.timeInMillis

        val diff = endMillis - nowMillis
        if (diff <= 0) return "Ended"

        val days = diff / (1000 * 60 * 60 * 24)
        val hours = (diff / (1000 * 60 * 60)) % 24

        return when {
            days > 0 -> "Ends in ${days}d ${hours}h"
            hours > 0 -> "Ends in ${hours}h"
            else -> "Ending soon"
        }

    } catch (e: Exception) {
        return "Ends: Unknown"
    }
}

