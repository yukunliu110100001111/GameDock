package com.example.gamedock.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/**
 * Displays Epic's profile page inside an in-app WebView and injects the stored access token.
 */
class EpicProfileActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var authHeaders: Map<String, String>

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
        if (accessToken.isNullOrBlank()) {
            finish()
            return
        }

        authHeaders = mapOf("Authorization" to "bearer $accessToken")

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val targetUrl = request?.url?.toString() ?: return false
                    view?.loadUrl(targetUrl, authHeaders)
                    return true
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url.isNullOrBlank()) return false
                    view?.loadUrl(url, authHeaders)
                    return true
                }
            }
        }

        setContentView(webView)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(PROFILE_URL, authHeaders)
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

    companion object {
        const val EXTRA_ACCESS_TOKEN = "extra_access_token"
        private const val PROFILE_URL = "https://www.epicgames.com/account/personal"
    }
}
