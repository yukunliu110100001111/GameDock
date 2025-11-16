package com.example.gamedock.data.model.account

import com.example.gamedock.data.model.PlatformType

sealed class PlatformAccount {
    abstract val id: String                // 用户 ID（steamId、epicId）
    abstract val nickname: String          // 昵称
    abstract val avatar: String            // 头像 URL
    abstract val platform: PlatformType    // Steam / Epic / etc
}

