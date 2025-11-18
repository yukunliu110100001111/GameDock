package com.example.gamedock.ui.home

import com.example.gamedock.data.model.PlatformType

class EpicProfileActivity : AccountWebViewActivity() {

    override fun resolveConfig(): WebViewConfig? {
        val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID) ?: return null
        return WebViewConfig(
            platform = PlatformType.Epic,
            accountId = accountId,
            targetUrl = PROFILE_URL
        )
    }

    companion object {
        const val EXTRA_ACCOUNT_ID = "extra_account_id"
        private const val PROFILE_URL = "https://www.epicgames.com/account/personal"
    }
}
