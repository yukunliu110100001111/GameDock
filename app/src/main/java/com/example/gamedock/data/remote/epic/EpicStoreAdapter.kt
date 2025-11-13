package com.example.gamedock.data.remote.epic

import com.example.gamedock.data.model.Freebie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpicStoreAdapter @Inject constructor(
    private val api: EpicApiService
) {
    suspend fun fetchFreebies(): List<Freebie> {
        val response = api.getFreeGames()

        val elements = response.data
            ?.Catalog
            ?.searchStore
            ?.elements ?: return emptyList()


        val freebies = mutableListOf<Freebie>()

        for (item in elements){
            val freeGames = item.promotions
                ?.promotionalOffers
                ?.firstOrNull()
                ?.promotionalOffers
                ?.firstOrNull()

            if (freeGames == null) continue

            val slug = item.productSlug
                ?: item.catalogNs?.mappings?.firstOrNull()?.pageSlug
                ?: item.urlSlug
                ?: continue

            val claimUrl = "https://store.epicgames.com/p/$slug"

            val imageUrl = item.keyImages
                ?.firstOrNull { it.type == "DieselStoreFrontWide" }
                ?.url
                ?: item.keyImages?.firstOrNull()?.url

            freebies.add(
                Freebie(
                    id = item.id ?: "",
                    title = item.title ?: "Unknown",
                    store = "Epic Games",
                    imageUrl = imageUrl,
                    claimUrl = claimUrl,
                    endDate = freeGames.endDate
                )
            )
        }
        return freebies
    }

}