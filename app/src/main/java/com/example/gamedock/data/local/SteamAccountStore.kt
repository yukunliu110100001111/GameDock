package com.example.gamedock.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gamedock.data.model.account.SteamAccount
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
        // Upsert a single Steam account in encrypted prefs.
        val list = loadAll(context).toMutableList()

        // Replace any existing account with the same steamId
        list.removeAll { it.id == account.id }
        list.add(account)

        saveList(context, list)
    }

    fun loadAll(context: Context): List<SteamAccount> {
        // Deserialize all stored Steam accounts (or empty list if none).
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
                    nickname = o.optString("nickname", "Steam User"),
                    avatar = o.optString("avatar", ""),
                    cookies = readCookies(o, o.getString("id"), o.getString("secure"), o.getString("sessionid"))
                )
            )
        }
        return result
    }

    fun saveList(context: Context, list: List<SteamAccount>) {
        // Persist the full account list atomically.
        val arr = JSONArray()
        list.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("id", acc.id)
                    put("secure", acc.steamLoginSecure)
                    put("sessionid", acc.sessionid)
                    put("nickname", acc.nickname)
                    put("cookies", JSONObject().apply {
                        val cookies = if (acc.cookies.isNotEmpty()) acc.cookies
                        else mapOf(
                            "steamLoginSecure" to acc.steamLoginSecure,
                            "sessionid" to acc.sessionid
                        )
                        cookies.forEach { (name, value) ->
                            put(name, value)
                        }
                    })
                    put("avatar", acc.avatar)
                }
            )
        }

        prefs(context).edit()
            .putString(KEY_ACCOUNTS, arr.toString())
            .apply()
    }

    fun delete(context: Context, steamId: String) {
        // Remove an account by steamId.
        val list = loadAll(context).toMutableList()
        list.removeAll { it.id == steamId }
        saveList(context, list)
    }

    private fun readCookies(
        jsonObject: JSONObject,
        accountId: String,
        steamLoginSecure: String,
        sessionId: String
    ): Map<String, String> {
        val cookiesObj = jsonObject.optJSONObject("cookies") ?: return mapOf(
            "steamLoginSecure" to steamLoginSecure,
            "sessionid" to sessionId
        )

        val result = mutableMapOf<String, String>()
        cookiesObj.keys().forEach { key ->
            result[key] = cookiesObj.optString(key)
        }

        if (!result.containsKey("steamLoginSecure")) {
            result["steamLoginSecure"] = steamLoginSecure
        }
        if (!result.containsKey("sessionid")) {
            result["sessionid"] = sessionId
        }

        return result
    }
}
