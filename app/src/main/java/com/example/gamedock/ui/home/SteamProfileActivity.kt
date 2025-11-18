package com.example.gamedock.ui.home

import com.example.gamedock.data.model.PlatformType

class SteamProfileActivity : AccountWebViewActivity() {

    override fun resolveConfig(): WebViewConfig? {
        val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID) ?: return null
        val url = intent.getStringExtra(EXTRA_PROFILE_URL)
            ?: "https://steamcommunity.com/profiles/$accountId"

        return WebViewConfig(
            platform = PlatformType.Steam,
            accountId = accountId,
            targetUrl = url
        )
    }

    companion object {
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
        const val EXTRA_PROFILE_URL = "extra_profile_url"
    }
}
