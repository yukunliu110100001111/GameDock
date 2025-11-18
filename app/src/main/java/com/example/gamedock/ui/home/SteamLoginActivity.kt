package com.example.gamedock.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import java.util.HashMap

/**
 * WebView-based Steam login flow that captures the required cookies
 * (steamLoginSecure + sessionid) once the user signs in successfully.
 */
class SteamLoginActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private val cookieManager: CookieManager = CookieManager.getInstance()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    checkCookies()
                }
            }
        }

        setContentView(webView)

        clearCookies()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(STEAM_LOGIN_URL)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) {
            webView.saveState(outState)
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }

    private fun checkCookies() {
        val cookies = mutableMapOf<String, String>()
        DOMAINS.forEach { domain ->
            val cookieStr = cookieManager.getCookie(domain) ?: return@forEach
            cookieStr.split(";").forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val value = parts[1].trim()
                    cookies[name] = value
                }
            }
        }

        val steamLoginSecure = cookies[COOKIE_STEAM_LOGIN_SECURE]
        val sessionId = cookies[COOKIE_SESSION_ID]

        if (!steamLoginSecure.isNullOrBlank() && !sessionId.isNullOrBlank()) {
            deliverResult(steamLoginSecure, sessionId, cookies)
        }
    }

    private fun deliverResult(
        steamLoginSecure: String,
        sessionId: String,
        cookies: Map<String, String>
    ) {
        val resultIntent = Intent().apply {
            putExtra(EXTRA_STEAM_LOGIN_SECURE, steamLoginSecure)
            putExtra(EXTRA_SESSION_ID, sessionId)
            putExtra(EXTRA_COOKIES, HashMap(cookies))
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun clearCookies() {
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies {
                cookieManager.flush()
            }
        } else {
            @Suppress("DEPRECATION")
            cookieManager.removeAllCookie()
        }
    }

    companion object {
        const val EXTRA_STEAM_LOGIN_SECURE = "extra_steam_login_secure"
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_COOKIES = "extra_cookies"

        private const val STEAM_LOGIN_URL = "https://store.steampowered.com/login/"
        private const val COOKIE_STEAM_LOGIN_SECURE = "steamLoginSecure"
        private const val COOKIE_SESSION_ID = "sessionid"
        private val DOMAINS = listOf(
            "https://store.steampowered.com",
            "https://steamcommunity.com"
        )
    }
}
