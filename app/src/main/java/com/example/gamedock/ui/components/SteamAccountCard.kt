package com.example.gamedock.ui.components
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.gamedock.data.model.SteamAccount

@Composable
fun SteamAccountCard(
    account: SteamAccount,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick(account.id) }
    ) {
        Row(
            modifier = Modifier
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

                Spacer(modifier = Modifier.width(16.dp))

                Column {

                    // ⭐ 昵称（新加）
                    Text(
                        text = account.nickname.ifBlank { "Steam User" },
                        style = MaterialTheme.typography.titleMedium
                    )

                    // ⭐ SteamID（第二行）
                    Text(
                        text = "ID: ${account.id}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ⭐ 删除按钮
            IconButton(
                onClick = { onDelete(account.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除账号",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}