package com.example.gamedock.data.remote.gamerpower

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.remote.dto.GamerPowerDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamerPowerStoreAdapter @Inject constructor(
    private val api: GamerPowerApiService
) {

    suspend fun fetchFreebies(): List<Freebie> {
        val dtos = api.getGiveaways(type = "game")
            .filterNot { it.platforms?.contains("Epic", ignoreCase = true) == true }

        val freebies = mutableListOf<Freebie>()

        for (dto in dtos) {

            // GamerPower 提供的领取 URL
            val claimUrl = dto.openGiveawayUrl ?: continue

            val storeName = extractStore(dto.platforms)
            val imageUrl = dto.image ?: dto.thumbnail

            freebies.add(
                Freebie(
                    id = dto.id.toString(),
                    title = dto.title,
                    store = storeName,
                    imageUrl = imageUrl,
                    claimUrl = claimUrl,
                    startDate = dto.publishedDate,
                    endDate = dto.endDate,
                    isClaimed = false
                )
            )
        }

        return freebies
    }

    /**
     * GamerPower 的 platforms 字段类似：
     * "PC, Steam"
     * "PC, DRM-Free"
     * "PC, Epic Games"
     * "PC, GOG"
     */
    private fun extractStore(platforms: String?): String {
        val p = platforms?.lowercase() ?: return "Unknown"
        return when {
            "steam" in p -> "Steam"
            "gog" in p -> "GOG"
//            "ubisoft" in p -> "Ubisoft"
//            "indiegala" in p -> "IndieGala"
//            "drm-free" in p -> "DRM-Free"
            else -> "Other"
        }
    }
}
