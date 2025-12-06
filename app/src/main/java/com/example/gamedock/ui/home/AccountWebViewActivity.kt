package com.example.gamedock.ui.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import android.webkit.CookieManager
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.RenderProcessGoneDetail
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.gamedock.data.model.PlatformType
import com.example.gamedock.data.repository.AccountCredentialsProvider
import com.example.gamedock.data.repository.model.AccountCredentials
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@AndroidEntryPoint
abstract class AccountWebViewActivity : ComponentActivity() {

    data class WebViewConfig(
        val platform: PlatformType,
        val accountId: String,
        val targetUrl: String
    )

    @Inject lateinit var credentialsProvider: AccountCredentialsProvider

    private lateinit var webView: WebView
    private lateinit var backButton: ImageButton
    private var headers: Map<String, String> = emptyMap()
    private var targetUrl: String = ""
    private var renderRestartAttempts = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        webView = createWebView()

        backButton = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_menu_revert)
            background = null
            val size = (48 * resources.displayMetrics.density).toInt()
            val margin = (12 * resources.displayMetrics.density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.START or Gravity.TOP).apply {
                setMargins(margin, margin, margin, margin)
            }
            setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        }

        container.addView(webView)
        container.addView(backButton)
        setContentView(container)

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
            return
        }

        val config = resolveConfig() ?: run {
            finish()
            return
        }
        targetUrl = config.targetUrl

        lifecycleScope.launch {
            val credentials = credentialsProvider.getCredentials(config.platform, config.accountId)
            if (credentials == null) {
                finish()
                return@launch
            }
            clearCookies(credentials.platform)
            applyCredentials(credentials)
            webView.loadUrl(config.targetUrl, headers)
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

    protected abstract fun resolveConfig(): WebViewConfig?

    private suspend fun clearCookies(platform: PlatformType) {
        if (platform != PlatformType.Steam) return
        val cookieManager = CookieManager.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            suspendCancellableCoroutine { continuation ->
                cookieManager.removeAllCookies {
                    cookieManager.flush()
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            cookieManager.removeAllCookie()
        }
    }

    private fun applyCredentials(credentials: AccountCredentials) {
        when (credentials.platform) {
            PlatformType.Epic -> {
                val token = credentials.accessToken
                if (!token.isNullOrBlank()) {
                    headers = mapOf("Authorization" to "bearer $token")
                }
            }

            PlatformType.Steam -> {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                credentials.cookies.forEach { (name, value) ->
                    val sanitized = sanitizeCookieValue(value)
                    cookieManager.setCookie(STEAM_COMMUNITY, "$name=$sanitized; path=/; secure")
                    cookieManager.setCookie(STEAM_STORE, "$name=$sanitized; path=/; secure")
                }
                cookieManager.flush()
                headers = emptyMap()
            }
        }
    }

    private fun sanitizeCookieValue(value: String): String {
        if (value.isEmpty()) return value
        return if (value.contains('%')) value else Uri.encode(value, COOKIE_ALLOWED_CHARS)
    }

    private fun recreateWebView() {
        if (!::webView.isInitialized) return
        renderRestartAttempts += 1
        if (renderRestartAttempts > 2) {
            // Avoid infinite restart loop; fall back to finish gracefully.
            finish()
            return
        }
        val parent = webView.parent as? ViewGroup
        parent?.removeView(webView)
        webView.destroy()

        webView = createWebView()
        (parent ?: window.decorView as? ViewGroup)?.addView(webView)
        if (targetUrl.isNotBlank()) {
            webView.loadUrl(targetUrl, headers)
        }
    }

    private fun createWebView(): WebView {
        return WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    recreateWebView()
                    return true
                }
            }
        }
    }

    companion object {
        private const val STEAM_COMMUNITY = "https://steamcommunity.com"
        private const val STEAM_STORE = "https://store.steampowered.com"
        private const val COOKIE_ALLOWED_CHARS = "-_.~%+=:@"
    }
}
