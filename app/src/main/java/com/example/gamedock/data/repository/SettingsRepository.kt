package com.example.gamedock.data.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val notificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
    val epicAutoRefreshEnabled: Flow<Boolean>
    suspend fun setEpicAutoRefresh(enabled: Boolean)
}
