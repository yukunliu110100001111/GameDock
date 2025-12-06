package com.example.gamedock.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SettingsRepository {

    private val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    private val keyNotifications = "notifications_enabled"
    private val keyEpicRefresh = "epic_auto_refresh"

    private val _notifications = MutableStateFlow(
        prefs.getBoolean(keyNotifications, true)
    )
    override val notificationsEnabled: Flow<Boolean> = _notifications.asStateFlow()

    private val _epicAutoRefresh = MutableStateFlow(
        prefs.getBoolean(keyEpicRefresh, true)
    )
    override val epicAutoRefreshEnabled: Flow<Boolean> = _epicAutoRefresh.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        // Persist global notification toggle and update flow.
        prefs.edit().putBoolean(keyNotifications, enabled).apply()
        _notifications.value = enabled
    }

    override suspend fun setEpicAutoRefresh(enabled: Boolean) {
        // Persist Epic token auto-refresh preference and update flow.
        prefs.edit().putBoolean(keyEpicRefresh, enabled).apply()
        _epicAutoRefresh.value = enabled
    }
}
