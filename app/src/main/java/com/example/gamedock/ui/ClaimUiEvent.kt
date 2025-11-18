package com.example.gamedock.ui

import com.example.gamedock.data.model.PlatformType

sealed class ClaimUiEvent {

    data class NoAccount(val platform: PlatformType) : ClaimUiEvent()

    data class OpenWebView(
        val accountId: String,
        val url: String,
        val platform: PlatformType
    ) : ClaimUiEvent()

    data class ExternalBrowser(val url: String) : ClaimUiEvent()
}
