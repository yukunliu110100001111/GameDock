package com.example.gamedock.ui.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun AccountDetailRouterScreen(
    navController: NavController,
    platform: String,
    id: String
) {
    // Simple router to platform-specific account detail screens.
    when (platform) {
        "Steam" -> SteamAccountDetailScreen(navController, id)
        "Epic" -> EpicAccountDetailScreen(navController, id)
    }
}
