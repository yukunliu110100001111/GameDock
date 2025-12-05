package com.example.gamedock.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    val isLowest = offer.currentPrice <= offer.lowestPrice && offer.lowestPrice > 0
    
    // 爱心颜色动画
    val heartColor by animateColorAsState(
        targetValue = if (isWatchlisted) Color(0xFFFF4D4D) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "HeartTint"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp), // 【关键修改】固定高度，强制变扁
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // --- 1. 左侧图片区 (变宽了) ---
            Box(
                modifier = Modifier
                    .width(120.dp) // 【关键修改】宽度增加，能看到更多画面
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!offer.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = offer.imageUrl,
                        contentDescription = offer.gameTitle,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // 裁剪填满，看起来更有质感
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Img", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // --- 2. 右侧内容容器 ---
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(10.dp) // 内部边距
            ) {
                // 中间：文本信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // 商店标签
                        StoreChip(store = offer.store)
                        Spacer(modifier = Modifier.height(4.dp))
                        // 标题
                        Text(
                            text = offer.gameTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                    
                    // 底部小字
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

                // 右边：价格与操作按钮
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 价格
                    Text(
                        text = CurrencyUtils.format(offer.currentPrice, offer.currencyCode),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // 底部操作区：爱心 + 跳转 (并排防拥挤)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 爱心按钮
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

                        // 跳转按钮 (做成小圆角矩形)
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

// 辅助组件：商店标签 (微调得更小一点)
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

// 颜色逻辑 (保持不变)
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