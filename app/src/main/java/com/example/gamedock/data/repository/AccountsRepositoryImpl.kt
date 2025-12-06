package com.example.gamedock.data.repository

import android.content.Context
import com.example.gamedock.data.local.EpicAccountStore
import com.example.gamedock.data.local.SteamAccountStore
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.model.account.EpicAccount
import com.example.gamedock.data.model.account.PlatformAccount
import com.example.gamedock.data.model.account.SteamAccount
import com.example.gamedock.data.remote.SteamApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Singleton
class AccountsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AccountsRepository {

    override suspend fun loadAllAccounts(): List<PlatformAccount> = withContext(Dispatchers.IO) {
        // Load accounts from local storage and enrich with avatars/nicknames where possible.
        // --- Steam: try to enrich nickname/avatar on demand ---
        val steam = SteamAccountStore.loadAll(context).map { acc ->
            val baseName = stripCdata(acc.nickname).ifBlank { "Steam User" }
            val baseAvatar = acc.avatar

            if (baseAvatar.isNotBlank() && baseName.isNotBlank() && baseName != "Steam User") {
                return@map acc.copy(nickname = baseName)
            }

            val profile = SteamApi.fetchSteamProfile(acc.id)

            val updated = if (profile != null) {
                val (rawName, rawAvatar) = profile
                val name = stripCdata(rawName)
                val avatarUrl = stripCdata(rawAvatar)
                val fallback = buildFallbackAvatar(name.ifBlank { baseName })
                acc.copy(
                    nickname = name.ifBlank { baseName },
                    avatar = avatarUrl.ifBlank { fallback }
                )
            } else {
                // fallback to generated avatar so UI always has an image
                val fallback = baseAvatar.ifBlank { buildFallbackAvatar(baseName) }
                acc.copy(
                    nickname = baseName,
                    avatar = fallback
                )
            }

            if (updated != acc) {
                // persist so next load is instant
                SteamAccountStore.saveAccount(context, updated)
            }
            updated
        }

        // --- Epic: generate fallback avatar (API token currently doesn't return avatar) ---
        val epic = EpicAccountStore.loadAll(context).map { acc ->
            val name = stripCdata(acc.nickname).ifBlank { "Epic User" }
            val avatar = stripCdata(acc.avatar).ifBlank { buildFallbackAvatar(name) }
            val updated = acc.copy(nickname = name, avatar = avatar)
            if (updated != acc) {
                EpicAccountStore.saveAccount(context, updated)
            }
            updated
        }

        (steam + epic)
    }

    override suspend fun saveSteamAccount(account: SteamAccount) = withContext(Dispatchers.IO) {
        // Persist or replace a Steam account entry.
        SteamAccountStore.saveAccount(context, account)
    }

    override suspend fun saveEpicAccount(account: EpicAccount) = withContext(Dispatchers.IO) {
        // Persist or replace an Epic account entry.
        EpicAccountStore.saveAccount(context, account)
    }

    override suspend fun deleteSteamAccount(id: String) = withContext(Dispatchers.IO) {
        // Remove a Steam account by id.
        SteamAccountStore.delete(context, id)
    }

    override suspend fun deleteEpicAccount(id: String) = withContext(Dispatchers.IO) {
        // Remove an Epic account by id.
        EpicAccountStore.delete(context, id)
    }

    override suspend fun findAccount(platform: PlatformType, id: String): PlatformAccount? =
        withContext(Dispatchers.IO) {
            // Lookup a specific account from local storage.
            when (platform) {
                PlatformType.Steam -> SteamAccountStore.loadAll(context).find { it.id == id }
                PlatformType.Epic -> EpicAccountStore.loadAll(context).find { it.id == id }
            }
        }

    /**
     * Build a neutral fallback avatar so the home card always has an image even
     * when the platform API does not expose avatars (Epic) or network fails.
     */
    private fun buildFallbackAvatar(name: String): String {
        val safe = URLEncoder.encode(name.ifBlank { "Player" }, StandardCharsets.UTF_8.toString())
        return "https://ui-avatars.com/api/?name=$safe&background=4F46E5&color=fff&bold=true"
    }

    private fun stripCdata(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace("<!\\[CDATA\\[".toRegex(), "")
            .replace("]]>", "")
            .trim()
    }
}
