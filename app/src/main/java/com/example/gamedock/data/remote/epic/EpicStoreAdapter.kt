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

        for (item in elements) {

            val activeFreeOffer = item.promotions
                ?.promotionalOffers
                ?.flatMap { it.promotionalOffers ?: emptyList() }
                ?.firstOrNull { it.discountSetting?.discountPercentage == 0 }

            val upcomingFreeOffer = item.promotions
                ?.upcomingPromotionalOffers
                ?.flatMap { it.promotionalOffers ?: emptyList() }
                ?.firstOrNull { it.discountSetting?.discountPercentage == 0 }

            val freeOffer = activeFreeOffer ?: upcomingFreeOffer ?: continue


            val price = item.price?.totalPrice

            val isActiveFree = price != null &&
                    price.discountPrice == 0 &&
                    (price.originalPrice ?: 0) > 0

            val isUpcomingFree = item.promotions
                ?.upcomingPromotionalOffers
                ?.any { wrapper ->
                    wrapper.promotionalOffers?.any { promo ->
                        promo.discountSetting?.discountPercentage == 0
                    } == true
                } == true

            val isFree = isActiveFree || isUpcomingFree

            if (!isFree) continue


            val slug = item.productSlug
                ?: item.catalogNs?.mappings?.firstOrNull()?.pageSlug
                ?: item.urlSlug
                ?: continue

            val claimUrl = "https://store.epicgames.com/p/$slug"

            val imageUrl = item.keyImages
                ?.firstOrNull { it.type == "DieselStoreFrontWide" }
                ?.url
                ?: item.keyImages?.firstOrNull()?.url


            val startDate = freeOffer.startDate
            val endDate = freeOffer.endDate


            freebies.add(
                Freebie(
                    id = item.id ?: "",
                    title = item.title ?: "Unknown",
                    store = "Epic Games",
                    imageUrl = imageUrl,
                    claimUrl = claimUrl,
                    startDate = startDate,   // important for filtering active freebies
                    endDate = endDate        // used to determine expiration
                )
            )
        }

        return freebies
    }
}
