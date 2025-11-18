// com/example/gamedock/data/remote/EpicOAuthConfig.kt
package com.example.gamedock.data.remote

object EpicOAuthConfig {

    // Epic Games Launcher client_id / client_secret (same pair used by Legendary)
    const val CLIENT_ID = "34a02cf8f4414e29b15921876da36f9a"
    const val CLIENT_SECRET = "daafbccc737745039dffe53d94fc76cf"

    // Default language and region (adjust if needed)
    const val LANGUAGE = "en"
    const val COUNTRY = "US"

    // OAuth host
    const val OAUTH_HOST =
        "https://account-public-service-prod03.ol.epicgames.com"

    const val REDIRECT_BASE = "https://www.epicgames.com/id/api/redirect"

    /**
     * Build the Legendary-style login URL.
     *
     * When this URL is opened, the user signs in on Epic's site.
     * After a successful login, Epic redirects to /id/api/redirect?code=xxx,
     * which we intercept inside the WebView to extract the authorization code.
     */
    fun buildLoginUrl(): String {
        // Same as Legendary: redirectUrl points to Epic's own redirect endpoint.
        val redirectUrl =
            "$REDIRECT_BASE?clientId=$CLIENT_ID&responseType=code"

        // Outer login page
        return "https://www.epicgames.com/id/login?redirectUrl=" +
                java.net.URLEncoder.encode(redirectUrl, "UTF-8")
    }
}
