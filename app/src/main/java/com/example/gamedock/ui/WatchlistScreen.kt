package com.example.gamedock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.gamedock.data.local.entity.WatchlistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamedock.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ----------------------------------------------------
//                Watchlist Screen
// ----------------------------------------------------
@Composable
fun WatchlistScreen(
    onOpenCompare: (String) -> Unit,
    viewModel: WatchlistViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Watchlist",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        if (items.isEmpty()) {
            Text(
                "Your watchlist is empty.",
                style = MaterialTheme.typography.bodyMedium
            )
            return
        }

        LazyColumn {
            items(items) { item ->
                WatchlistCard(
                    item = item,
                    onDelete = { viewModel.delete(it) },
                    onOpenCompare = onOpenCompare
                )
            }
        }
    }
}

// ----------------------------------------------------
//                Watchlist Card
// ----------------------------------------------------
@Composable
private fun WatchlistCard(
    item: WatchlistEntity,
    onDelete: (String) -> Unit,
    onOpenCompare: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onOpenCompare(item.title) },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Img", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 文本区域
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                Spacer(Modifier.height(6.dp))

                // 监控的平台标签
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.preferredStores.forEach { store ->
                        PreferredStoreChip(store)
                    }
                }
            }

            // 删除按钮
            IconButton(onClick = { onDelete(item.gameId) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

// ----------------------------------------------------
//       Preferred Store Chip (复用颜色体系)
// ----------------------------------------------------
@Composable
private fun PreferredStoreChip(store: String) {
    val bg = MaterialTheme.colorScheme.primary
    Surface(
        color = bg.copy(alpha = 0.14f),
        shape = RoundedCornerShape(50),
        tonalElevation = 0.dp
    ) {
        Text(
            text = store,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = bg
        )
    }
}

// ----------------------------------------------------
//                  ViewModel
// ----------------------------------------------------
@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val repo: WatchlistRepository
) : ViewModel() {

    val items: StateFlow<List<WatchlistEntity>> =
        repo.watchlistFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )

    fun delete(gameId: String) {
        viewModelScope.launch {
            repo.remove(gameId)
        }
    }
}
