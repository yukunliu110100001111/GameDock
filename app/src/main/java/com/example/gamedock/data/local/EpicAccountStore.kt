package com.example.gamedock.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.gamedock.data.model.account.EpicAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EpicAccountStore {

    private const val PREF_NAME = "epic_accounts"
    private const val KEY_JSON = "accounts_json"

    private val gson = Gson()

    private fun prefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Load all stored Epic accounts.
     */
    fun loadAll(context: Context): List<EpicAccount> {
        // Read from encrypted prefs; return empty list on parse failure.
        val json = prefs(context).getString(KEY_JSON, "[]") ?: "[]"
        val type = object : TypeToken<List<EpicAccount>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Persist the full account list (internal use only).
     */
    fun saveList(context: Context, list: List<EpicAccount>) {
        // Overwrite stored list with the provided snapshot.
        prefs(context).edit().putString(KEY_JSON, gson.toJson(list)).apply()
    }

    /**
     * Save or replace a single account entry.
     */
    fun saveAccount(context: Context, account: EpicAccount) {
        // Upsert entry keyed by id.
        val list = loadAll(context).toMutableList()
        list.removeAll { it.id == account.id } // avoid duplicates
        list.add(account)
        saveList(context, list)
    }

    /**
     * Delete an account by id.
     */
    fun delete(context: Context, epicId: String) {
        // Remove matching account and rewrite list.
        val list = loadAll(context).filterNot { it.id == epicId }
        saveList(context, list)
    }
}
