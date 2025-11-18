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
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.ui.Screen
import kotlinx.coroutines.launch

@Composable
fun EpicAccountDetailScreen(
    navController: androidx.navigation.NavController,
    epicId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(Screen.Home.route)
    }
    val vm: HomeViewModel = hiltViewModel(parentEntry)

    LaunchedEffect(Unit) {
        if (vm.accounts.value.isEmpty()) {
            vm.loadAllAccounts()
        }
    }

    val accounts by vm.accounts.collectAsState()

    val account = accounts
        .filterIsInstance<EpicAccount>()
        .find { it.id == epicId }

    if (account == null) {
        Text("找不到 Epic 账号")
        return
    }

    var status by remember { mutableStateOf("") }

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
        Text("Epic ID: ${account.id}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))

        if (status.isNotEmpty()) {
            Text(status, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        // 打开 Epic 官方个人页面
        Button(
            onClick = {
                val intent = Intent(context, EpicProfileActivity::class.java).apply {
                    putExtra(EpicProfileActivity.EXTRA_ACCESS_TOKEN, account.accessToken)
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("打开 Epic 个人资料页面")
        }

        Spacer(Modifier.height(16.dp))

        // 刷新 Token
        Button(
            onClick = {
                scope.launch {
                    status = "刷新 Token 中..."
                    val success = vm.refreshEpicAccount(account)
                    status = if (success) "Token 已刷新！" else "刷新失败，请稍后再试"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("刷新 Epic Token")
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
