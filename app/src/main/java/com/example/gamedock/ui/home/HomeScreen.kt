package com.example.gamedock.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gamedock.ui.Screen
import com.example.gamedock.ui.components.AccountCard
import com.example.gamedock.ui.components.AddAccountCard

@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val vm: HomeViewModel = viewModel()
    val accounts by vm.accounts.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadAllAccounts(context)
    }

    LazyColumn(Modifier.padding(16.dp)) {

        items(accounts) { account ->

            AccountCard(
                account = account,

                onClick = { acc ->
                    navController.navigate("${Screen.AccountDetail.route}/${acc.platform}/${acc.id}")
                },

                onDelete = { acc ->
                    vm.deleteAccount(context, acc)
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