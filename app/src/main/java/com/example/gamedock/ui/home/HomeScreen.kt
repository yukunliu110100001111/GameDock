package com.example.gamedock.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.gamedock.ui.Screen
import com.example.gamedock.ui.components.AccountCard
import com.example.gamedock.ui.components.AddAccountCard

@Composable
fun HomeScreen(navController: NavController) {

    val vm: HomeViewModel = hiltViewModel()
    val accounts by vm.accounts.collectAsState()
    val homeEntry = remember(navController) { navController.getBackStackEntry(Screen.Home.route) }
    val accountAdded by homeEntry.savedStateHandle
        .getStateFlow("account_added", false)
        .collectAsState()

    LaunchedEffect(Unit) {
        vm.loadAllAccounts()
    }

    LaunchedEffect(accountAdded) {
        if (accountAdded) {
            vm.loadAllAccounts(force = true)
            homeEntry.savedStateHandle["account_added"] = false
        }
    }

    LazyColumn(Modifier.padding(16.dp)) {

        items(accounts) { account ->

            AccountCard(
                account = account,

                onClick = { acc ->
                    navController.navigate("${Screen.AccountDetail.route}/${acc.platform}/${acc.id}")
                },

                onDelete = { acc ->
                    vm.deleteAccount(acc)
                }
            )
        }

        item {
            AddAccountCard {
                navController.navigate(Screen.AddAccount.route)
            }
        }
    }
}
