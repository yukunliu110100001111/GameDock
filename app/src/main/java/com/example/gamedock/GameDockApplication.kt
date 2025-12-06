package com.example.gamedock

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Configuration.Provider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.hilt.work.HiltWorkerFactory
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import com.example.gamedock.data.local.EpicAccountStore
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.repository.EpicAuthRepository
import com.example.gamedock.data.repository.AccountsRepository
import com.example.gamedock.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Application class required by Hilt.
 */
@HiltAndroidApp
class GameDockApplication : Application(), Provider, ImageLoaderFactory {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var accountsRepository: AccountsRepository
    @Inject lateinit var epicAuthRepository: EpicAuthRepository
    @Inject lateinit var settingsRepository: SettingsRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        refreshEpicTokensOnLaunch()
    }

    /**
     * Use a slower-but-steady client for image loading to avoid Steam CDN timeouts.
     */
    override fun newImageLoader(): ImageLoader {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(client)
            .crossfade(true)
            .build()
    }

    /**
     * Refresh all stored Epic accounts once on app launch so access tokens are warm.
     */
    private fun refreshEpicTokensOnLaunch() {
        appScope.launch {
            val enabled = runCatching { settingsRepository.epicAutoRefreshEnabled.first() }
                .getOrDefault(true)
            if (!enabled) return@launch

            val epicAccounts = EpicAccountStore.loadAll(this@GameDockApplication)
            epicAccounts.forEach { account ->
                var attempts = 0
                while (attempts < 2) {
                    attempts++
                    val refreshed = runCatching {
                        epicAuthRepository.refreshTokens(account.refreshToken)
                    }.getOrNull()

                    if (refreshed != null) {
                        val updated: EpicAccount = account.copy(
                            accessToken = refreshed.accessToken,
                            refreshToken = refreshed.refreshToken,
                            nickname = account.nickname // keep existing display name
                        )
                        accountsRepository.saveEpicAccount(updated)
                        break
                    } else if (attempts < 2) {
                        // brief delay before retry
                        kotlinx.coroutines.delay(1500)
                    } else {
                        Log.w("EpicTokenRefresh", "Failed to refresh token for ${account.id}")
                    }
                }
            }
        }
    }
}
