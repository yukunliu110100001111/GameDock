package com.example.gamedock.data.model.account

import com.example.gamedock.data.model.PlatformType

data class SteamAccount(
    override val id: String,
    val steamLoginSecure: String,
    val sessionid: String,
    val cookies: Map<String, String> = emptyMap(),
    override var nickname: String = "Steam User",
    override var avatar: String = "",
) : PlatformAccount() {
    override val platform = PlatformType.Steam
}
