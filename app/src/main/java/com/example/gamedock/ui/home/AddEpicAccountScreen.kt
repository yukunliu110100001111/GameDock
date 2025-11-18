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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun AddEpicAccountScreen(
    navController: NavController,
    viewModel: AddEpicAccountViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("account_added", true)
            viewModel.resetCompletionFlag()
            navController.popBackStack()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra(EpicLoginActivity.EXTRA_AUTH_CODE)
            if (!code.isNullOrBlank()) {
                viewModel.completeAuthorization(code)
            } else {
                viewModel.onMissingCode()
            }
        } else {
            viewModel.onLoginCancelled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Epic Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        Text(uiState.statusMessage)

        uiState.errorMessage?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val intent = Intent(context, EpicLoginActivity::class.java)
                launcher.launch(intent)
            },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isProcessing) "Working..." else "Open Epic Login")
        }

        if (uiState.isProcessing) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
        }
    }
}
