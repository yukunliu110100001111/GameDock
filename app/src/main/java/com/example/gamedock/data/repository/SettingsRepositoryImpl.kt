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

    private val _notifications = MutableStateFlow(
        prefs.getBoolean(keyNotifications, true)
    )
    override val notificationsEnabled: Flow<Boolean> = _notifications.asStateFlow()

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(keyNotifications, enabled).apply()
        _notifications.value = enabled
    }
}
