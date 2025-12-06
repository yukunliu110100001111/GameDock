package com.example.gamedock.ui.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.ui.Screen
import com.example.gamedock.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
        Text("Epic account not found.")
        return
    }

    var status by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account.nickname.ifBlank { "Epic Account" }) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val context = LocalContext.current
            val avatarRequest = remember(account.avatar) {
                ImageRequest.Builder(context)
                    .data(account.avatar)
                    .listener(onError = { _, result ->
                        Log.e(
                            "AvatarLoad",
                            "Failed to load Epic avatar for ${account.id} from ${account.avatar}",
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
            Text("Epic ID: ${account.id}", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))

            if (status.isNotEmpty()) {
                Text(status, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val intent = Intent(context, EpicProfileActivity::class.java).apply {
                        putExtra(EpicProfileActivity.EXTRA_ACCOUNT_ID, account.id)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Epic Profile")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        status = "Refreshing token..."
                        val success = vm.refreshEpicAccount(account)
                        status = if (success) "Token refreshed." else "Refresh failed, please try again."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Epic Token")
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
