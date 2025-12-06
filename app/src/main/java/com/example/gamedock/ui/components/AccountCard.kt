package com.example.gamedock.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamedock.R
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.PlatformAccount
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
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically // 保证整体垂直居中
        ) {

            // --- 左侧内容区域 (头像 + 文字) ---
            // 使用 weight(1f) 占据剩余空间，防止挤压右侧按钮
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val name = stripCdata(account.nickname).ifBlank { account.platformName() }
                val avatarUrl = account.avatar.takeIf { it.isNotBlank() }
                    ?: buildFallbackAvatar(name)
                val context = LocalContext.current
                var avatarFailed by remember { mutableStateOf(false) }
                var retryKey by remember { mutableStateOf(0) }

                val avatarRequest = remember(avatarUrl, retryKey) {
                    ImageRequest.Builder(context)
                        .data(avatarUrl)
                        .setParameter("retryKey", retryKey)
                        .listener(onError = { _, result ->
                            avatarFailed = true
                            Toast.makeText(
                                context,
                                "Avatar load failed, tap to retry",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e(
                                "AvatarLoad",
                                "Failed to load avatar for ${account.id} from $avatarUrl",
                                result.throwable
                            )
                        })
                        .build()
                }

                // Avatar with simple retry tap when failed
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                    .clickable(enabled = avatarFailed) {
                        avatarFailed = false
                        retryKey++
                    },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = avatarRequest,
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_foreground),
                        modifier = Modifier.fillMaxSize()
                    )
                    if (avatarFailed) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    // Display name
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1, // 限制行数
                        overflow = TextOverflow.Ellipsis // 超出显示省略号
                    )

                    // Platform label
                    Text(
                        text = when (account.platform) {
                            PlatformType.Steam -> "Steam · ID: ${account.id}"
                            PlatformType.Epic -> "Epic · ID: ${account.id}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, // 限制行数，防止 Epic ID 撑开
                        overflow = TextOverflow.Ellipsis // 超出显示省略号
                    )
                }
            }

            // --- 右侧内容区域 (删除按钮) ---
            // 因为左侧加了 weight，这里不需要额外处理，它会保持固定大小
            IconButton(onClick = { onDelete(account) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete account",
                    tint = Color.Black
                )
            }
        }
    }
}

// 辅助函数保持不变
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
