package com.example.gamedock.data.repository

import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.account.SteamAccount

interface AccountsRepository {
    suspend fun loadAllAccounts(): List<PlatformAccount>
    suspend fun saveSteamAccount(account: SteamAccount)
    suspend fun saveEpicAccount(account: EpicAccount)
    suspend fun deleteSteamAccount(id: String)
    suspend fun deleteEpicAccount(id: String)
    suspend fun findAccount(platform: PlatformType, id: String): PlatformAccount?

    suspend fun deleteAccount(account: PlatformAccount) {
        when (account.platform) {
            PlatformType.Steam -> deleteSteamAccount(account.id)
            PlatformType.Epic -> deleteEpicAccount(account.id)
        }
    }
}
