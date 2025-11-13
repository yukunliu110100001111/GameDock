package com.example.gamedock.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gamedock.ui.Dimens
import com.example.gamedock.ui.Screen
import com.example.gamedock.ui.components.AddAccountCard
import com.example.gamedock.ui.components.SteamAccountCard

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    // 监听添加账号结果
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle
            ?.getLiveData<Boolean>("account_added")
            ?.observeForever { added ->
                if (added) {
                    viewModel.refresh(context)
                    savedStateHandle.set("account_added", false)
                }
            }
    }

    // 初次进入页面加载账号
    LaunchedEffect(true) {
        viewModel.loadAccounts(context)
    }

    val accounts by viewModel.accounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.screenPadding)
    ) {
        Text(
            text = "Welcome to GameDock!",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Manage your Steam accounts, compare prices, and more.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = Dimens.cardSpacing)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            items(accounts) { account ->
                SteamAccountCard(
                    account = account,
                    onClick = { id ->
                        navController.navigate("${Screen.AccountDetail.route}/$id")
                    },
                    onDelete = { id ->
                        viewModel.deleteAccount(context = context, steamId = id)
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