package com.example.gamedock.ui.home

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gamedock.ui.Screen

@Composable
fun AddSteamAccountScreen(
    navController: NavController,
    viewModel: AddSteamAccountViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val homeEntry = remember(navController) { navController.getBackStackEntry(Screen.Home.route) }

    var secure by remember { mutableStateOf("") }
    var sessionId by remember { mutableStateOf("") }
    var capturedCookies by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            homeEntry.savedStateHandle["account_added"] = true
            viewModel.resetSavedFlag()
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    val loginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val secureCookie = result.data?.getStringExtra(SteamLoginActivity.EXTRA_STEAM_LOGIN_SECURE)
            val sessionCookie = result.data?.getStringExtra(SteamLoginActivity.EXTRA_SESSION_ID)
            val cookiesMap = (result.data?.getSerializableExtra(SteamLoginActivity.EXTRA_COOKIES) as? HashMap<String, String>)?.toMap()
            if (!secureCookie.isNullOrBlank() && !sessionCookie.isNullOrBlank()) {
                secure = secureCookie
                sessionId = sessionCookie
                capturedCookies = cookiesMap ?: emptyMap()
                viewModel.saveAccount(secureCookie, sessionCookie, capturedCookies)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
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
                val cookies = if (capturedCookies.isNotEmpty()) capturedCookies else mapOf(
                    "steamLoginSecure" to secure,
                    "sessionid" to sessionId
                )
                viewModel.saveAccount(secure, sessionId, cookies)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            enabled = secure.isNotBlank() && sessionId.isNotBlank() && !uiState.isSaving
        ) {
            Text(if (uiState.isSaving) "Saving..." else "Save Account")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val intent = Intent(context, SteamLoginActivity::class.java)
                loginLauncher.launch(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            enabled = !uiState.isSaving
        ) {
            Text("Sign in via Steam Web")
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
