// com/example/gamedock/data/remote/EpicAuthApi.kt
package com.example.gamedock.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Credentials
import org.json.JSONObject

object EpicAuthApi {

    private val client = OkHttpClient()

    /**
     * Step 1 — 用 authorization_code 换 token
     *
     * POST https://account-public-service-prod03.ol.epicgames.com/account/api/oauth/token
     *
     * grant_type=authorization_code
     * code=...
     * token_type=eg1
     * Auth: Basic base64(client_id:client_secret)
     */
    suspend fun exchangeAuthCodeForToken(authCode: String): JSONObject? =
        withContext(Dispatchers.IO) {

            val form = FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", authCode)
                .add("token_type", "eg1")
                .build()

            val basic = Credentials.basic(
                EpicOAuthConfig.CLIENT_ID,
                EpicOAuthConfig.CLIENT_SECRET
            )

            val request = Request.Builder()
                .url("${EpicOAuthConfig.OAUTH_HOST}/account/api/oauth/token")
                .post(form)
                .header("Authorization", basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            runCatching {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_AUTH", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }.getOrNull()
        }

    /**
     * Step 2 — 使用 refresh_token 刷新 access_token
     *
     * grant_type=refresh_token
     */
    suspend fun refreshToken(refreshToken: String): JSONObject? =
        withContext(Dispatchers.IO) {

            val form = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("token_type", "eg1")
                .build()

            val basic = Credentials.basic(
                EpicOAuthConfig.CLIENT_ID,
                EpicOAuthConfig.CLIENT_SECRET
            )

            val request = Request.Builder()
                .url("${EpicOAuthConfig.OAUTH_HOST}/account/api/oauth/token")
                .post(form)
                .header("Authorization", basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            runCatching {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_REFRESH", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }.getOrNull()
        }

    /**
     * 可选：验证当前 access_token 是否有效
     */
    suspend fun verifyAccessToken(accessToken: String): JSONObject? =
        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url("${EpicOAuthConfig.OAUTH_HOST}/account/api/oauth/verify")
                .get()
                .header("Authorization", "bearer $accessToken")
                .build()

            runCatching {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_VERIFY", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }.getOrNull()
        }
}