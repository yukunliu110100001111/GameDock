package com.example.gamedock.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamedock.ui.Screen
import com.example.gamedock.data.model.PlatformType

@Composable
fun AddAccountScreen(navController: NavController) {

    // Simple menu to choose which platform account to add.
    val platforms = listOf(
        PlatformType.Steam,
        PlatformType.Epic,
        // Additional platforms can be added here later
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Add Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        platforms.forEach { platform ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        when (platform) {
                            PlatformType.Steam -> navController.navigate(Screen.AddSteam.route)
                            PlatformType.Epic -> navController.navigate(Screen.AddEpic.route)
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(platform.displayName, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
