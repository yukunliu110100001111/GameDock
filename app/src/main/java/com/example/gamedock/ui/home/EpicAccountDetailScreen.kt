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
        Text("Epic account not found.")
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

        // Open Epic profile page
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

        // Refresh token
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

        // Delete account
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
