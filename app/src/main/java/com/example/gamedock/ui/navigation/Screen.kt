package com.example.gamedock.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Freebies : Screen("freebies", "Freebies", Icons.Filled.CardGiftcard)
    data object Compare : Screen("compare", "Compare", Icons.Filled.AttachMoney)
    data object Watchlist : Screen("watchlist", "Watchlist", Icons.Filled.Star)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Freebies, Compare, Watchlist, Settings)
    }
}
