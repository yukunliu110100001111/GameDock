package com.example.gamedock.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.gamedock.ui.home.AccountDetailRouterScreen
import com.example.gamedock.ui.home.AccountScreen
import com.example.gamedock.ui.home.AddAccountScreen
import com.example.gamedock.ui.home.AddEpicAccountScreen
import com.example.gamedock.ui.home.HomeScreen
import com.example.gamedock.ui.home.AddSteamAccountScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Freebies.route) { FreebiesScreen() }
        composable(
            route = "compare?query={query}",
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            CompareScreen(
                queryFromRoute = backStackEntry.arguments?.getString("query") ?: ""
            )
        }


        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onOpenCompare = { query ->
                    navController.navigate("compare?query=$query")
                }
            )
        }
        composable(Screen.Bundles.route) {
            BundlesScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.AddAccount.route) {
            AddAccountScreen(navController)
        }
        composable(Screen.AddSteam.route) {
            AddSteamAccountScreen(navController)
        }
        composable(Screen.AddEpic.route) {
            AddEpicAccountScreen(navController)
        }


        composable(
            route = "account_detail/{platform}/{id}"
        ) { backStackEntry ->

            val platform = backStackEntry.arguments?.getString("platform")!!
            val id = backStackEntry.arguments?.getString("id")!!

            AccountDetailRouterScreen(
                navController = navController,
                platform = platform,
                id = id
            )
        }

    }
}
