package com.example.gamedock.data.repository

import com.example.gamedock.data.remote.EpicAuthApi
import com.example.gamedock.data.repository.model.EpicAuthTokens
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class EpicAuthRepositoryImpl @Inject constructor() : EpicAuthRepository {

    override suspend fun exchangeAuthCode(authorizationCode: String): EpicAuthTokens? =
        withContext(Dispatchers.IO) {
            // Code -> token exchange, then verify access token to fetch display name.
            val raw = EpicAuthApi.exchangeAuthCodeForToken(authorizationCode) ?: return@withContext null
            val tokens = raw.toTokens() ?: return@withContext null
            val profile = EpicAuthApi.verifyAccessToken(tokens.accessToken)
            tokens.copy(displayName = profile?.optString("displayName"))
        }

    override suspend fun refreshTokens(refreshToken: String): EpicAuthTokens? =
        withContext(Dispatchers.IO) {
            // Refresh existing tokens using a stored refresh_token.
            val raw = EpicAuthApi.refreshToken(refreshToken) ?: return@withContext null
            raw.toTokens()
        }

    private fun JSONObject.toTokens(): EpicAuthTokens? {
        val access = optString("access_token")
        val refresh = optString("refresh_token")
        val accountId = optString("account_id")
        return if (access.isNotBlank() && refresh.isNotBlank() && accountId.isNotBlank()) {
            EpicAuthTokens(
                accountId = accountId,
                accessToken = access,
                refreshToken = refresh
            )
        } else {
            null
        }
    }
}
