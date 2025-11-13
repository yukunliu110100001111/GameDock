package com.example.gamedock.data.remote.epic

import com.example.gamedock.data.model.Freebie

class EpicStoreAdapter(
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

            val slug = item.productSlug ?:continue
            val claimUrl = "https://store.epicgames.com/p/$slug"


            freebies.add(
                Freebie(
                    id = item.id ?: "",
                    title = item.title ?: "Unknown",
                    store = "Epic Games",
                    claimUrl = claimUrl,
                    endDate = freeGames.endDate
                )
            )
        }
        return freebies
    }

}