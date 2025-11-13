package com.example.gamedock.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gamedock.data.model.SteamAccount
import org.json.JSONArray
import org.json.JSONObject

object SteamAccountStore {

    private const val PREF_NAME = "steam_accounts"

    private const val KEY_ACCOUNTS = "accounts_json"

    private fun prefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveAccount(context: Context, account: SteamAccount) {
        val list = loadAll(context).toMutableList()

        // 如果已有同 steamId 的账号，覆盖
        list.removeAll { it.id == account.id }
        list.add(account)

        saveList(context, list)
    }

    fun loadAll(context: Context): List<SteamAccount> {
        val json = prefs(context).getString(KEY_ACCOUNTS, null) ?: return emptyList()
        val arr = JSONArray(json)

        val result = mutableListOf<SteamAccount>()

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            result.add(
                SteamAccount(
                    id = o.getString("id"),
                    steamLoginSecure = o.getString("secure"),
                    sessionid = o.getString("sessionid"),
                    nickname = o.optString("nickname", "Steam User")
                )
            )
        }
        return result
    }

    fun saveList(context: Context, list: List<SteamAccount>) {
        val arr = JSONArray()
        list.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("id", acc.id)
                    put("secure", acc.steamLoginSecure)
                    put("sessionid", acc.sessionid)
                    put("nickname", acc.nickname)
                }
            )
        }

        prefs(context).edit()
            .putString(KEY_ACCOUNTS, arr.toString())
            .apply()
    }

    fun deleteAccount(context: Context, steamId: String) {
        val list = loadAll(context).toMutableList()
        list.removeAll { it.id == steamId }
        saveList(context, list)
    }
}