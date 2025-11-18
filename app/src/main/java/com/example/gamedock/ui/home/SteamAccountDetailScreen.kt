package com.example.gamedock.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.ui.Screen

@Composable
fun SteamAccountDetailScreen(
    navController: androidx.navigation.NavController,
    steamId: String
) {
    val context = LocalContext.current
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Screen.Home.route)
    }
    val vm: HomeViewModel = hiltViewModel(parentEntry)

    LaunchedEffect(Unit) {
        if (vm.accounts.value.isEmpty()) {
            vm.loadAllAccounts()
        }
    }

    // 从 ViewModel 中找到当前账号
    val accounts by vm.accounts.collectAsState()
    val account = accounts
        .filterIsInstance<SteamAccount>()
        .find { it.id == steamId }

    if (account == null) {
        Text("找不到 Steam 账号")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = rememberAsyncImagePainter(account.avatar),
            contentDescription = null,
            modifier = Modifier.size(96.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(account.nickname, style = MaterialTheme.typography.headlineSmall)
        Text("Steam ID: ${account.id}", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))

        // 打开 Steam 个人资料
        Button(
            onClick = {
                val intent = Intent(context, SteamProfileActivity::class.java).apply {
                    putExtra(
                        SteamProfileActivity.EXTRA_PROFILE_URL,
                        "https://steamcommunity.com/profiles/${account.id}"
                    )
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("打开 Steam 个人资料")
        }

        Spacer(Modifier.height(16.dp))

        // 删除账号
        Button(
            onClick = {
                vm.deleteAccount(account)
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("删除账号")
        }
    }
}
