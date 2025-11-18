package com.example.gamedock.data.model

data class Freebie(
    val id: String,
    val title: String,
    val store: String,
    val imageUrl: String? = null,
    val claimUrl: String?,
    val startDate: String? = null,
    val endDate: String? = null,
    val isClaimed: Boolean = false
)

fun Freebie.platformType(): PlatformType? {
    return when (store.lowercase()) {
        "epic", "epic games", "epic games store" -> PlatformType.Epic
        "steam" -> PlatformType.Steam
//        "gog" -> PlatformType.GOG
//        "ubisoft", "uplay", "ubisoft connect" -> PlatformType.Ubisoft
        else -> null
    }
}
