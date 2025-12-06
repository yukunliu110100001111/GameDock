// com/example/gamedock/data/remote/EpicAuthApi.kt
package com.example.gamedock.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Credentials
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object EpicAuthApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Step 1 — Exchange authorization_code for tokens.
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

            runWithRetry(tag = "EPIC_AUTH") {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_AUTH", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }
        }

    /**
     * Step 2 — Refresh tokens using refresh_token.
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

            runWithRetry(tag = "EPIC_REFRESH") {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_REFRESH", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }
        }

    /**
     * Optional: verify whether the current access_token is valid.
     */
    suspend fun verifyAccessToken(accessToken: String): JSONObject? =
        withContext(Dispatchers.IO) {

            val request = Request.Builder()
                .url("${EpicOAuthConfig.OAUTH_HOST}/account/api/oauth/verify")
                .get()
                .header("Authorization", "bearer $accessToken")
                .build()

            runWithRetry(tag = "EPIC_VERIFY") {
                client.newCall(request).execute().use { resp ->
                    val raw = resp.body?.string() ?: "null"

                    if (!resp.isSuccessful) {
                        android.util.Log.e("EPIC_VERIFY", "HTTP ${resp.code}\n$raw")
                        return@use null
                    }

                    JSONObject(raw)
                }
            }
        }

    private inline fun <T> runWithRetry(
        tag: String,
        maxAttempts: Int = 2,
        block: () -> T?
    ): T? {
        var lastError: Throwable? = null
        repeat(maxAttempts) { attempt ->
            runCatching { return block() }
                .onFailure { lastError = it }
            Log.w(tag, "attempt ${attempt + 1} failed: ${lastError?.message}")
        }
        lastError?.let { Log.e(tag, "all attempts failed", it) }
        return null
    }
}
