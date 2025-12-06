package com.example.gamedock.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.PlatformType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AccountCard(
    account: PlatformAccount,
    onClick: (PlatformAccount) -> Unit,
    onDelete: (PlatformAccount) -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(account) }
    ) {

        Row(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row {

                val name = stripCdata(account.nickname).ifBlank { account.platformName() }
                val avatarUrl = account.avatar.takeIf { it.isNotBlank() }
                    ?: buildFallbackAvatar(name)

                // Avatar
                Image(
                    painter = rememberAsyncImagePainter(avatarUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                Column {

                    // Display name
                    Text(
                        name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Platform label
                    Text(
                        when (account.platform) {
                            PlatformType.Steam -> "Steam · ID: ${account.id}"
                            PlatformType.Epic -> "Epic · ID: ${account.id}"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Delete action
            IconButton(onClick = { onDelete(account) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete account",
                    tint = androidx.compose.ui.graphics.Color.Black
                )
            }
        }
    }
}

private fun stripCdata(text: String?): String {
    if (text.isNullOrBlank()) return ""
    return text.replace("<!\\[CDATA\\[".toRegex(), "")
        .replace("]]>", "")
        .trim()
}

private fun PlatformAccount.platformName(): String {
    return when (platform) {
        PlatformType.Steam -> "Steam User"
        PlatformType.Epic -> "Epic User"
    }
}

private fun buildFallbackAvatar(name: String): String {
    val safe = URLEncoder.encode(name.ifBlank { "Player" }, StandardCharsets.UTF_8.toString())
    return "https://ui-avatars.com/api/?name=$safe&background=4F46E5&color=fff&bold=true"
}
