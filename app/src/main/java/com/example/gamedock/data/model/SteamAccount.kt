package com.example.gamedock.data.model

data class SteamAccount(
    val id: String, // steamId64
    val steamLoginSecure: String,
    val sessionid: String,
    var nickname: String = "Unknown",
    var avatar: String = ""
)