package com.example.gamedock.data.repository

import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.repository.model.AccountCredentials

interface AccountCredentialsProvider {
    suspend fun getCredentials(platform: PlatformType, accountId: String): AccountCredentials?
}
