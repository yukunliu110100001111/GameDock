package com.example.gamedock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gamedock.data.repository.DealsRepository

@Composable
fun NavGraph(
    navController: NavHostController,
    repository: DealsRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Freebies.route) {
            FreebiesScreen(repository = repository)
        }
        composable(Screen.Compare.route) {
            CompareScreen(repository = repository)
        }
        composable(Screen.Watchlist.route) {
            WatchlistScreen()
        }
        composable(Screen.Bundles.route) {
            BundlesScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
