package com.example.gamedock.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import com.example.gamedock.data.remote.EpicOAuthConfig

/**
 * Minimal WebView-based login flow that mirrors Legendary's browser login.
 * Loads Epic's login page, intercepts the redirect URL, and returns the auth code.
 */
class EpicLoginActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private var completed = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val uri = request?.url ?: return false
                    return handleRedirect(uri.toString())
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                    return if (handleRedirect(url)) true
                    else super.shouldOverrideUrlLoading(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    if (completed) return
                    if (url != null && url.startsWith(EpicOAuthConfig.REDIRECT_BASE)) {
                        view?.evaluateJavascript(AUTH_CODE_SCRIPT) { value ->
                            if (completed) return@evaluateJavascript
                            val code = value
                                ?.trim()
                                ?.removeSurrounding("\"")
                                ?.takeIf { !it.isNullOrBlank() && it != "null" }
                            if (!code.isNullOrBlank()) {
                                deliverCode(code)
                            }
                        }
                    }
                }
            }
        }

        setContentView(webView)

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(EpicOAuthConfig.buildLoginUrl())
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

    private fun handleRedirect(url: String): Boolean {
        if (completed) return true
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false

        val isEpicRedirect = url.startsWith(EpicOAuthConfig.REDIRECT_BASE)
        val isLocalHost = uri.host == "localhost"

        if (!isEpicRedirect && !isLocalHost) return false

        val code = uri.getQueryParameter("code")
        if (!code.isNullOrBlank()) {
            deliverCode(code)
            return true
        }

        // Let the page load so we can extract the code from the JSON response body.
        return isLocalHost
    }

    private fun deliverCode(code: String) {
        if (completed) return
        completed = true
        val resultIntent = Intent().apply {
            putExtra(EXTRA_AUTH_CODE, code)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    companion object {
        const val EXTRA_AUTH_CODE = "extra_auth_code"
        private val AUTH_CODE_SCRIPT = """
            (function(){
                try{
                    var data = JSON.parse(document.body.innerText);
                    if (data.authorizationCode) { return data.authorizationCode; }
                    if (data.redirectUrl) {
                        try {
                            var url = new URL(data.redirectUrl);
                            return url.searchParams.get('code');
                        } catch(e) {}
                    }
                    return null;
                }catch(e){
                    return null;
                }
            })();
        """.trimIndent()
    }
}
