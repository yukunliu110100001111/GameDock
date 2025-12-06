package com.example.gamedock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamedock.R
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.util.CurrencyUtils

@Composable
fun PriceCard(
    offer: Offer,
    isWatchlisted: Boolean = false,
    onOpenStore: (Offer) -> Unit = {},
    onToggleWatchlist: (Offer) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val isLowest = offer.currentPrice <= offer.lowestPrice && offer.lowestPrice > 0

    val heartColor by animateColorAsState(
        targetValue = if (isWatchlisted) Color(0xFFFF4D4D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "HeartTint"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!offer.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(offer.imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = offer.gameTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Img", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        StoreChip(store = offer.store)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = offer.gameTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                    
                    if (isLowest) {
                        Text(
                            text = "History Low",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    } else if (offer.lowestPrice > 0) {
                        Text(
                            text = "Low: ${CurrencyUtils.format(offer.lowestPrice, offer.currencyCode)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = CurrencyUtils.format(offer.currentPrice, offer.currencyCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleWatchlist(offer) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isWatchlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Watchlist",
                                tint = heartColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Surface(
                            onClick = {
                                onOpenStore(offer)
                                offer.url?.let { uriHandler.openUri(it) }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Store",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// Store tag badge
@Composable
private fun StoreChip(store: String) {
    val bg = storeColor(store)
    Surface(
        color = bg.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = store.uppercase(),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = bg.darken(0.1f)
        )
    }
}

// Store color palette
@Composable
private fun storeColor(store: String): Color {
    return when {
        store.contains("Steam", true) -> Color(0xFF1B6FBC)
        store.contains("Epic", true) -> Color(0xFF333333)
        store.contains("GOG", true) -> Color(0xFF673AB7)
        store.contains("Ubisoft", true) -> Color(0xFF0059C4)
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
fun PriceCardFlatPreview() {
    val sampleOffer = Offer(
        id = "offer-steam",
        gameTitle = "Cyberpunk 2077: Phantom Liberty",
        store = "Steam",
        currentPrice = 29.99,
        lowestPrice = 19.99,
        currencyCode = "USD",
        url = "https://store.steampowered.com",
        imageUrl = "https://via.placeholder.com/300x400"
    )
    PriceCard(offer = sampleOffer, isWatchlisted = true)
}
