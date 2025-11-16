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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.PlatformType

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

                // ⭐ 头像
                Image(
                    painter = rememberAsyncImagePainter(account.avatar),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(16.dp))

                Column {

                    // ⭐ 昵称
                    Text(
                        account.nickname,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // ⭐ 平台标识
                    Text(
                        when (account.platform) {
                            PlatformType.Steam -> "Steam · ID: ${account.id}"
                            PlatformType.Epic -> "Epic · ID: ${account.id}"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ⭐ 删除
            IconButton(onClick = { onDelete(account) }) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        }
    }
}
