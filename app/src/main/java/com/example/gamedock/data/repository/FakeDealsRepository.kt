package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Game
import com.example.gamedock.data.model.Offer
import kotlinx.coroutines.delay

/**
 * In-memory repository that exposes the mock data used by the demo screens.
 */
class FakeDealsRepository : DealsRepository {

    private val mockFreebies = listOf(
        Freebie(
            id = "freebie-1",
            title = "Alan Wake Remastered",
            store = "Epic Games Store",
            claimUrl = "https://store.epicgames.com/p/alan-wake-remastered",
            isClaimed = false
        ),
        Freebie(
            id = "freebie-2",
            title = "Call of Juarez: Gunslinger",
            store = "Steam",
            claimUrl = "https://store.steampowered.com/app/204450",
            isClaimed = false
        ),
        Freebie(
            id = "freebie-3",
            title = "Ghostrunner Demo",
            store = "GOG",
            claimUrl = "https://www.gog.com/game/ghostrunner_demo",
            isClaimed = false
        )
    )


    private val mockOffers = listOf(
        Offer(
            id = "offer-steam",
            gameTitle = "Cyberpunk 2077",
            store = "Steam",
            currentPrice = 19.99,
            lowestPrice = 9.99
        ),
        Offer(
            id = "offer-epic",
            gameTitle = "Cyberpunk 2077",
            store = "Epic Games Store",
            currentPrice = 18.49,
            lowestPrice = 8.99
        ),
        Offer(
            id = "offer-gog",
            gameTitle = "Cyberpunk 2077",
            store = "GOG",
            currentPrice = 21.99,
            lowestPrice = 10.49
        ),
        Offer(
            id = "offer-ubi",
            gameTitle = "Cyberpunk 2077",
            store = "Ubisoft Connect",
            currentPrice = 20.49,
            lowestPrice = 9.49
        )
    )

    override suspend fun getFreebies(): List<Freebie> {
        delay(250) // Simulate network latency
        return mockFreebies
    }

    override suspend fun comparePrices(query: String): List<Offer> {
        delay(250)
        if (query.isBlank()) return emptyList()
        return mockOffers.filter { offer ->
            offer.gameTitle.contains(query, ignoreCase = true) ||
                offer.store.contains(query, ignoreCase = true)
        }
    }
}
