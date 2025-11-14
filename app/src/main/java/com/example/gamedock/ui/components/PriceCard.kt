package com.example.gamedock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.util.CurrencyUtils

@Composable
fun PriceCard(offer: Offer, onOpenStore: (Offer) -> Unit = {}) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = offer.store,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lowest: ${CurrencyUtils.format(offer.lowestPrice, offer.currencyCode)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(offer.currentPrice, offer.currencyCode),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = {
                        onOpenStore(offer)
                        offer.url?.let { uriHandler.openUri(it) }
                    },
                    enabled = offer.url != null
                ) {
                    Text(if (offer.url != null) "Go to Store" else "No Link... QAQ")
                }
            }
        }
    }
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
        url = "https://store.steampowered.com/app/1091500/Cyberpunk_2077/"
    )
    PriceCard(offer = sampleOffer)
}
