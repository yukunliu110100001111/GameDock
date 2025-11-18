package com.example.gamedock.data.repository.model

data class EpicAuthTokens(
    val accountId: String,
    val accessToken: String,
    val refreshToken: String,
    val displayName: String? = null
)
