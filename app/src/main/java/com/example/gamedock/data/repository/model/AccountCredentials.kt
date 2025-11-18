package com.example.gamedock.data.repository.model

import com.example.gamedock.data.model.PlatformType

data class AccountCredentials(
    val platform: PlatformType,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val cookies: Map<String, String> = emptyMap()
)
