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
     * 读取所有 Epic 账号
     */
    fun loadAll(context: Context): List<EpicAccount> {
        val json = prefs(context).getString(KEY_JSON, "[]") ?: "[]"
        val type = object : TypeToken<List<EpicAccount>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * 保存完整账号列表（内部使用）
     */
    fun saveList(context: Context, list: List<EpicAccount>) {
        prefs(context).edit().putString(KEY_JSON, gson.toJson(list)).apply()
    }

    /**
     * 保存一个账号（追加到列表）
     */
    fun saveAccount(context: Context, account: EpicAccount) {
        val list = loadAll(context).toMutableList()
        list.removeAll { it.id == account.id } // 避免重复
        list.add(account)
        saveList(context, list)
    }

    /**
     * 删除账号
     */
    fun delete(context: Context, epicId: String) {
        val list = loadAll(context).filterNot { it.id == epicId }
        saveList(context, list)
    }
}
