package com.example.gamedock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.ui.home.AccountScreen
import com.example.gamedock.ui.home.HomeScreen
import com.example.gamedock.ui.home.AddSteamAccountScreen

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
            HomeScreen(navController = navController)
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
        composable(Screen.AddAccount.route) {
            AddSteamAccountScreen(navController)
        }

        composable("${Screen.AccountDetail.route}/{steamId}") { backStackEntry ->

            val steamId = backStackEntry.arguments?.getString("steamId")!!
            val context = navController.context

            // 找到该账号
            val account = SteamAccountStore
                .loadAll(context)
                .first { it.id == steamId }

            AccountScreen(
                navController = navController,
                account = account
            )
        }
    }
}
