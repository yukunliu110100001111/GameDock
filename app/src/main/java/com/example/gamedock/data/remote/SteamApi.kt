package com.example.gamedock.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request

object SteamApi {

    private val client = OkHttpClient()

    fun fetchSteamProfile(steamId: String): Pair<String, String>? {
        val url = "https://steamcommunity.com/profiles/$steamId/?xml=1"

        val req = Request.Builder().url(url).build()

        return try {
            val resp = client.newCall(req).execute()
            val xml = resp.body?.string() ?: return null

            val name = "<steamID>(.*?)</steamID>".toRegex()
                .find(xml)?.groupValues?.get(1) ?: "Unknown"

            val avatar = "<avatarFull>(.*?)</avatarFull>".toRegex()
                .find(xml)?.groupValues?.get(1) ?: ""

            name to avatar

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}