package com.example.gamedock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.ui.compare.CompareRoute
import com.example.gamedock.ui.freebies.FreebiesRoute
import com.example.gamedock.ui.home.HomeRoute
import com.example.gamedock.ui.settings.SettingsRoute
import com.example.gamedock.ui.watchlist.WatchlistRoute

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
            HomeRoute()
        }
        composable(Screen.Freebies.route) {
            FreebiesRoute(repository = repository)
        }
        composable(Screen.Compare.route) {
            CompareRoute(repository = repository)
        }
        composable(Screen.Watchlist.route) {
            WatchlistRoute()
        }
        composable(Screen.Settings.route) {
            SettingsRoute()
        }
    }
}
