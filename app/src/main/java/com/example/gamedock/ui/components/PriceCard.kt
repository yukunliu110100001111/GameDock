package com.example.gamedock.ui.components

import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.util.CurrencyUtils

@Composable
fun PriceCard(offer: Offer, onOpenStore: (Offer) -> Unit = {}, onAddWatchlist: (Offer)  -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    val diffPercent: Double? = if (offer.lowestPrice > 0 && offer.currentPrice > offer.lowestPrice) {
        ((offer.currentPrice - offer.lowestPrice) / offer.lowestPrice) * 100
    } else null
    val atLowest = offer.currentPrice == offer.lowestPrice

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
            // 封面图
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (!offer.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = offer.imageUrl,
                        contentDescription = offer.gameTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) { Text("No Img", style = MaterialTheme.typography.bodySmall) }
                }
            }

            // 右侧信息
            Column(modifier = Modifier.weight(1f)) {
                // Store + 当前价 行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StoreChip(store = offer.store)
                    Text(
                        text = CurrencyUtils.format(offer.currentPrice, offer.currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // 游戏标题弱化显示
                Text(
                    text = offer.gameTitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 最低价与状态
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lowest: ${CurrencyUtils.format(offer.lowestPrice, offer.currencyCode)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    when {
                        atLowest -> {
                            StatusBadge(text = "At Lowest", color = MaterialTheme.colorScheme.tertiaryContainer)
                        }
                        diffPercent != null -> {
                            StatusBadge(text = "+${"%.0f".format(diffPercent)}%", color = MaterialTheme.colorScheme.errorContainer)
                        }
                        offer.currentPrice < offer.lowestPrice -> {
                            StatusBadge(text = "Below Low", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                }
            }

            // 按钮区
            Column(horizontalAlignment = Alignment.End) {
                TextButton(
                    onClick = {
                        onOpenStore(offer)
                        offer.url?.let { uriHandler.openUri(it) }
                    },
                    enabled = offer.url != null
                ) {
                    Text(if (offer.url != null) "Store" else "No Link")
                }

                TextButton(
                    onClick = { onAddWatchlist(offer) }
                ) {
                    Text("Watchlist")
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = bg
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.22f),
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
private fun storeColor(store: String): Color {
    return when {
        store.contains("Steam", true) -> Color(0xFF1B6FBC)
        store.contains("Epic", true) -> Color(0xFF9146FF)
        store.contains("GOG", true) -> Color(0xFF673AB7)
        store.contains("Ubisoft", true) -> Color(0xFF2E82D8)
        store.contains("Humble", true) -> Color(0xFFE9642E)
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun Color.darken(factor: Float): Color {
    val r = (red * (1 - factor)).coerceIn(0f, 1f)
    val g = (green * (1 - factor)).coerceIn(0f, 1f)
    val b = (blue * (1 - factor)).coerceIn(0f, 1f)
    return Color(r, g, b, alpha)
}

@Preview
@Composable
fun PriceCardPreview() {
    val sampleOffer = Offer(
        id = "offer-steam",
        gameTitle = "Cyberpunk 2077",
        store = "Steam",
        currentPrice = 19.99,
        lowestPrice = 9.99,
        currencyCode = "USD",
        url = "https://store.steampowered.com/app/1091500/Cyberpunk_2077/",
        imageUrl = "https://via.placeholder.com/300x200.png?text=Cyberpunk"
    )
    PriceCard(offer = sampleOffer)
}
