package com.example.gamedock.ui.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.ui.Screen
import com.example.gamedock.R

@OptIn(ExperimentalMaterial3Api::class)
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
        if (vm.uiState.value.accounts.isEmpty()) {
            vm.loadAllAccounts()
        }
    }

    // Lookup the selected account in the shared ViewModel
    val accounts by vm.uiState.collectAsState()
    val account = accounts.accounts
        .filterIsInstance<SteamAccount>()
        .find { it.id == steamId }

    if (account == null) {
        Text("Steam account not found.")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account.nickname.ifBlank { "Steam Account" }) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val context = LocalContext.current
            val avatarRequest = remember(account.avatar) {
                ImageRequest.Builder(context)
                    .data(account.avatar)
                    .listener(onError = { _, result ->
                        Log.e(
                            "AvatarLoad",
                            "Failed to load Steam avatar for ${account.id} from ${account.avatar}",
                            result.throwable
                        )
                    })
                    .build()
            }

            AsyncImage(
                model = avatarRequest,
                contentDescription = null,
                placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                error = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(account.nickname, style = MaterialTheme.typography.headlineSmall)
            Text("Steam ID: ${account.id}", style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(context, SteamProfileActivity::class.java).apply {
                        putExtra(SteamProfileActivity.EXTRA_ACCOUNT_ID, account.id)
                        putExtra(
                            SteamProfileActivity.EXTRA_PROFILE_URL,
                            "https://steamcommunity.com/profiles/${account.id}"
                        )
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Steam Profile")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    vm.deleteAccount(account)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Account")
            }
        }
    }
}
