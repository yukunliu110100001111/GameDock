// com/example/gamedock/data/remote/EpicOAuthConfig.kt
package com.example.gamedock.data.remote

object EpicOAuthConfig {

    // Epic Games Launcher 的 client_id / client_secret
    // （就是 legendary 那对）
    const val CLIENT_ID = "34a02cf8f4414e29b15921876da36f9a"
    const val CLIENT_SECRET = "daafbccc737745039dffe53d94fc76cf"

    // 语言和地区（可按需要改）
    const val LANGUAGE = "en"
    const val COUNTRY = "US"

    // OAuth 服务器
    const val OAUTH_HOST =
        "https://account-public-service-prod03.ol.epicgames.com"

    const val REDIRECT_BASE = "https://www.epicgames.com/id/api/redirect"

    /**
     * 生成和 legendary 一样的登录 URL
     *
     * 打开这个 URL，让用户在 Epic 官方页面上登录。
     * 登录成功后，会重定向到 /id/api/redirect?code=xxx
     * 我们在 WebView 里拦截这个 URL，取出 code。
     */
    fun buildLoginUrl(): String {
        // 注意：和 legendary 一样，redirectUrl 指向 Epic 自己的 redirect endpoint
        val redirectUrl =
            "$REDIRECT_BASE?clientId=$CLIENT_ID&responseType=code"

        // 最外层登录页
        return "https://www.epicgames.com/id/login?redirectUrl=" +
                java.net.URLEncoder.encode(redirectUrl, "UTF-8")
    }
}
