package com.example.gamedock.data.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val notificationsEnabled: Flow<Boolean>
    suspend fun setNotificationsEnabled(enabled: Boolean)
}
