package com.example.gamedock.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.gamedock.ui.Screen
import com.example.gamedock.ui.components.AccountCard
import com.example.gamedock.ui.components.AddAccountCard

@Composable
fun HomeScreen(navController: NavController) {

    val vm: HomeViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
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

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        }
        uiState.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                androidx.compose.material3.Text(uiState.errorMessage!!)
                androidx.compose.material3.Button(
                    onClick = { vm.loadAllAccounts(force = true) },
                    modifier = Modifier.padding(top = 12.dp)
                ) { androidx.compose.material3.Text("Retry") }
            }
        }
        uiState.accounts.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                androidx.compose.material3.Text("No accounts yet.")
                androidx.compose.material3.Button(
                    onClick = { navController.navigate(Screen.AddAccount.route) },
                    modifier = Modifier.padding(top = 12.dp)
                ) { androidx.compose.material3.Text("Add account") }
            }
        }
        else -> {
            val useGrid = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
            if (useGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts) { account ->
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
            } else {
                LazyColumn(Modifier.padding(16.dp)) {

                    items(uiState.accounts) { account ->

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
        }
    }
}
