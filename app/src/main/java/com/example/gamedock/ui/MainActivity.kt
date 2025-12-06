package com.example.gamedock.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.notifications.PriceDropNotifier
import com.example.gamedock.workers.PriceCheckWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject notifier to create channel and dispatch alerts
    @Inject lateinit var notifier: PriceDropNotifier
    @Inject lateinit var dealsRepository: DealsRepository

    private val requestNotificationPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            // no action needed
        }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun schedulePriceCheckWorker() {
        val request = PeriodicWorkRequestBuilder<PriceCheckWorker>(
            6, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "price_check_worker",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()

        // Ensure notification channel exists
        notifier.createNotificationChannel()

        // Register periodic price check worker
        schedulePriceCheckWorker()

        // Prime freebies cache at launch for offline access
        lifecycleScope.launch {
            dealsRepository.getFreebies()
        }

        setContent {
            GameDockApp()
        }
    }
}
