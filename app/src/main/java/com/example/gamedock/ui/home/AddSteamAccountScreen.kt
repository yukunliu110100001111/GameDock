package com.example.gamedock.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.model.account.SteamAccount

@Composable
fun AddSteamAccountScreen(navController: NavController) {

    val context = LocalContext.current

    var secure by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add Steam Account",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = secure,
            onValueChange = { secure = it },
            label = { Text("steamLoginSecure") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = sessionId,
            onValueChange = { sessionId = it },
            label = { Text("sessionid") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Button(
            onClick = {
                // 保存账号
                val account = SteamAccount(
                    id = extractSteamId(secure),
                    steamLoginSecure = secure,
                    sessionid = sessionId,
                    nickname = "Steam User"
                )

                SteamAccountStore.saveAccount(context, account)

                // 给 HomeScreen 一个刷新信号
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("account_added", true)

                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Save Account")
        }
    }
}

fun extractSteamId(steamLoginSecure: String): String {
    return steamLoginSecure
        .split("%7C%7C", "||")
        .firstOrNull()
        ?: "Unknown"
}