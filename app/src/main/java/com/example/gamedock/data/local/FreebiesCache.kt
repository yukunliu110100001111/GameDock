package com.example.gamedock.data.local

import android.content.Context
import com.example.gamedock.data.model.Freebie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreebiesCache @Inject constructor(
    @ApplicationContext context: Context,
    private val gson: Gson
) {
    private val prefs = context.getSharedPreferences("freebies_cache", Context.MODE_PRIVATE)
    private val key = "freebies_json"
    private val type = object : TypeToken<List<Freebie>>() {}.type

    fun load(): List<Freebie> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return runCatching { gson.fromJson<List<Freebie>>(json, type) }.getOrElse { emptyList() }
    }

    fun save(list: List<Freebie>) {
        val json = gson.toJson(list, type)
        prefs.edit().putString(key, json).apply()
    }
}
