package com.example.gamedock.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamedock.data.model.account.SteamAccount


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    account: SteamAccount
) {
    val context = LocalContext.current
    var loginStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account.nickname.ifBlank { "Steam Account" }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // 🔽 这里放你原本的内容（头像、昵称、测试登录按钮等）


            Text(
                text = "Steam Account",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Steam ID:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(account.id)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "steamLoginSecure (前20位):",
                style = MaterialTheme.typography.titleMedium
            )
            Text(account.steamLoginSecure.take(20) + "...")

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "sessionid:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(account.sessionid)

            Spacer(modifier = Modifier.height(32.dp))

            // ⭐ 打开网站验证按钮
            Button(
                onClick = {
                    val url = "https://steamcommunity.com/profiles/${account.id}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("打开网站验证")
            }
        }
    }
}

