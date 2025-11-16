package com.example.gamedock.ui.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamedock.data.local.EpicAccountStore
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.remote.EpicAuthApi
import kotlinx.coroutines.launch

@Composable
fun AddEpicAccountScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var statusText by remember {
        mutableStateOf("点击下方按钮，在官方页面登录你的 Epic 账号。")
    }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(EpicLoginActivity.EXTRA_AUTH_CODE)
            if (!code.isNullOrBlank()) {
                scope.launch {
                    isProcessing = true
                    statusText = "授权中，请稍候..."

                    val tokenResult = EpicAuthApi.exchangeAuthCodeForToken(code)
                    if (tokenResult != null && tokenResult.has("access_token")) {
                        val accessToken = tokenResult.getString("access_token")
                        val refreshToken = tokenResult.getString("refresh_token")
                        val accountId = tokenResult.optString("account_id")

                        val verifyResult = EpicAuthApi.verifyAccessToken(accessToken)
                        val nickname = verifyResult?.optString("displayName")
                            ?: "Epic User"
                        val resolvedId = when {
                            accountId.isNotBlank() -> accountId
                            verifyResult?.has("account_id") == true ->
                                verifyResult.getString("account_id")
                            else -> accessToken.takeLast(16)
                        }

                        val account = EpicAccount(
                            id = resolvedId,
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            nickname = nickname,
                            avatar = ""
                        )

                        EpicAccountStore.saveAccount(context, account)

                        statusText = "账号已保存！"
                        isProcessing = false
                        navController.popBackStack()
                    } else {
                        statusText = "授权失败，请重试。"
                        isProcessing = false
                    }
                }
            } else {
                statusText = "未能获取授权 code，请重试。"
            }
        } else {
            statusText = "已取消 Epic 登录流程。"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("添加 Epic 账号", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Text(statusText)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, EpicLoginActivity::class.java)
                launcher.launch(intent)
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isProcessing) "处理中..." else "打开 Epic 登录")
        }

        if (isProcessing) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}
