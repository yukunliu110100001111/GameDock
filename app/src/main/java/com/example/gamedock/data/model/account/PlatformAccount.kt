package com.example.gamedock.data.model.account

import com.example.gamedock.data.model.PlatformType

sealed class PlatformAccount {
    abstract val id: String                // user identifier (steamId, epicId, etc.)
    abstract val nickname: String          // display name
    abstract val avatar: String            // avatar URL
    abstract val platform: PlatformType    // Steam / Epic / etc.
}
