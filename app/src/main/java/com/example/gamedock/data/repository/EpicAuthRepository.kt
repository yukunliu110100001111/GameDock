package com.example.gamedock.data.repository

import com.example.gamedock.data.repository.model.EpicAuthTokens

interface EpicAuthRepository {
    suspend fun exchangeAuthCode(authorizationCode: String): EpicAuthTokens?
    suspend fun refreshTokens(refreshToken: String): EpicAuthTokens?
}
