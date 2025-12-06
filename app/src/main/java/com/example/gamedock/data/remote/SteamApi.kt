package com.example.gamedock.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object SteamApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    fun fetchSteamProfile(steamId: String): Pair<String, String>? {
        val url = "https://steamcommunity.com/profiles/$steamId/?xml=1"

        val req = Request.Builder().url(url).build()

        return runCatching {
            client.newCall(req).execute().use { resp ->
                val xml = resp.body?.string() ?: return null

                val name = "<steamID>(.*?)</steamID>".toRegex()
                    .find(xml)?.groupValues?.get(1)
                    ?.let(::stripCdata)
                    ?: "Unknown"

                val avatar = "<avatarFull>(.*?)</avatarFull>".toRegex()
                    .find(xml)?.groupValues?.get(1)
                    ?.let(::stripCdata)
                    ?: ""

                name to avatar
            }
        }.onFailure { err ->
            Log.w("SteamApi", "fetchSteamProfile failed: ${err.message}")
        }.getOrNull()
    }

    private fun stripCdata(text: String?): String {
        if (text.isNullOrBlank()) return ""
        return text
            .replace("<!\\[CDATA\\[".toRegex(), "")
            .replace("]]>", "")
            .trim()
    }
}
