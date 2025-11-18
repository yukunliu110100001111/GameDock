package com.example.gamedock.data.repository

import android.content.Context
import com.example.gamedock.data.local.EpicAccountStore
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.account.SteamAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AccountsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AccountsRepository {

    override suspend fun loadAllAccounts(): List<PlatformAccount> = withContext(Dispatchers.IO) {
        val steam = SteamAccountStore.loadAll(context)
        val epic = EpicAccountStore.loadAll(context)
        (steam + epic)
    }

    override suspend fun saveSteamAccount(account: SteamAccount) = withContext(Dispatchers.IO) {
        SteamAccountStore.saveAccount(context, account)
    }

    override suspend fun saveEpicAccount(account: EpicAccount) = withContext(Dispatchers.IO) {
        EpicAccountStore.saveAccount(context, account)
    }

    override suspend fun deleteSteamAccount(id: String) = withContext(Dispatchers.IO) {
        SteamAccountStore.delete(context, id)
    }

    override suspend fun deleteEpicAccount(id: String) = withContext(Dispatchers.IO) {
        EpicAccountStore.delete(context, id)
    }

    override suspend fun findAccount(platform: PlatformType, id: String): PlatformAccount? =
        withContext(Dispatchers.IO) {
            when (platform) {
                PlatformType.Steam -> SteamAccountStore.loadAll(context).find { it.id == id }
                PlatformType.Epic -> EpicAccountStore.loadAll(context).find { it.id == id }
            }
        }
}
