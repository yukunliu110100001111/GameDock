package com.example.gamedock.data.remote

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object SteamApi {

    // Steam community XML can be slow; use a slightly higher timeout to avoid ReadTimeouts.
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    fun fetchSteamProfile(steamId: String): Pair<String, String>? {
        val url = "https://steamcommunity.com/profiles/$steamId/?xml=1"

        val req = Request.Builder().url(url).build()

        return runCatching {
            client.newCall(req).execute().use { resp ->
                val xml = resp.body?.string() ?: return null

                // The Steam XML wraps values in CDATA and includes newlines, so allow dots to
                // match across line breaks.
                val name = "<steamID>(.*?)</steamID>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
                    .find(xml)?.groupValues?.get(1)
                    ?.let(::stripCdata)
                    ?: "Unknown"

                val avatar = "<avatarFull>(.*?)</avatarFull>"
                    .toRegex(setOf(RegexOption.DOT_MATCHES_ALL))
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
