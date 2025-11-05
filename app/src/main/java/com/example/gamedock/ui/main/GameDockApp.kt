package com.example.gamedock.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.gamedock.core.design.Dimens
import com.example.gamedock.core.design.Strings
import com.example.gamedock.di.RepositoryModule
import com.example.gamedock.ui.navigation.BottomNavBar
import com.example.gamedock.ui.navigation.NavGraph

@Composable
fun GameDockApp() {
    val navController = rememberNavController()
    val dealsRepository = remember { RepositoryModule.provideDealsRepository() }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(Dimens.screenPadding)
            ) {
                Text(
                    text = Strings.appName,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            NavGraph(
                navController = navController,
                repository = dealsRepository
            )
        }
    }
}
