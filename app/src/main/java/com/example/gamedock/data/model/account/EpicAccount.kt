package com.example.gamedock.data.model.account

import com.example.gamedock.data.model.PlatformType

data class EpicAccount(
    override val id: String,
    val accessToken: String,
    val refreshToken: String,
    override var nickname: String = "Epic User",
    override var avatar: String = "",
) : PlatformAccount() {
    override val platform = PlatformType.Epic
}