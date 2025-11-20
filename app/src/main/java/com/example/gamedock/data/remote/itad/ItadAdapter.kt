package com.example.gamedock.data.remote.itad

import android.util.Log
import com.example.gamedock.data.model.BundleInfo
import com.example.gamedock.data.model.Offer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItadAdapter @Inject constructor(
    private val itadApiService: ItadApiService
) {

    private val ITAD_API_KEY = "d719761d720142c5f15a7b7b7177704783ffc227"

    private val countryCode = "US" // todo: make dynamic

    suspend fun comparePrices(gameQuery: String): List<Offer> {
        try {
            // search for the game to get its ID
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )

            val searchItem = searchResults.firstOrNull()

            val gameId = searchItem?.id
            val gameTitle = searchItem?.title

            // if no game found, return empty list
            if (gameId == null || gameTitle == null) {
                Log.w("ItadAdapter", "No game found for query: $gameQuery")
                return emptyList()
            }

            // get price details for the found game ID
            val gameIdsToQuery = listOf(gameId)

            val priceResponse = itadApiService.getGamePrices(
                apiKey = ITAD_API_KEY,
                country = countryCode,
                gameIds = gameIdsToQuery,
                onlyDeals = null, // null = fetch all prices, including the original price
                capacity = null   // null = no limit on the number of returned offers
            )

            val gameDetails = priceResponse.firstOrNull()

            if (gameDetails == null) {
                Log.w("ItadAdapter", "No price details found for game ID: $gameId")
                return emptyList()
            }

            return gameDetails.deals.map { deal ->
                // if storeLow is null, use current price as lowest price
                val storeLowestPrice = deal.storeLow?.amount ?: deal.price.amount

                Offer(
                    id = gameId,
                    gameTitle = gameTitle,
                    store = deal.shop.name,
                    currentPrice = deal.price.amount,
                    lowestPrice = storeLowestPrice,
                    currencyCode = deal.price.currency,
                    url = deal.url
                )
            }


        }
        catch (_: Exception) {
            return emptyList()
        }
    }

    suspend fun getPrice (gameQuery: String): List<Offer> {
        try {
            // search for the game to get its ID
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )

            val gameId = searchResults.firstOrNull()?.id

            // if no game found, return empty list
            if (gameId == null) {
                return emptyList()
            }

            // get price overview for the found game ID
            val gameIdsToQuery = listOf(gameId)

            val priceResponse = itadApiService.getGamePriceOverview(
                apiKey = ITAD_API_KEY,
                country = countryCode,
                gameIds = gameIdsToQuery
            )

            return mapResponseToOffer(
                response = priceResponse,
                gameId = gameId,
                gameTitle = searchResults.first().title
            )


        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun mapResponseToOffer(
        response: GamePriceResponse?,
        gameId: String,
        gameTitle: String
    ): List<Offer> {

        return response?.prices?.mapNotNull { priceInfo ->
            val currentPriceDetails = priceInfo.current ?: return@mapNotNull null
            val lowestPriceDetails = priceInfo.lowest

            Offer(
                id = gameId,
                gameTitle = gameTitle,

                store = currentPriceDetails.shop.name,
                currentPrice = currentPriceDetails.price.amount,

                // if no lowest price available, use current price
                lowestPrice = lowestPriceDetails?.price?.amount
                    ?: currentPriceDetails.price.amount,

                currencyCode = currentPriceDetails.price.currency,
                url = currentPriceDetails.url
            )
        } ?: emptyList()
    }

    suspend fun searchBundles(gameQuery: String): List<BundleInfo> {
        try {
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )

            val gameId = searchResults.firstOrNull()?.id ?: return emptyList()

            val bundlesResponse = itadApiService.getGameBundles(
                apiKey = ITAD_API_KEY,
                gameId = gameId,
                country = "US"
            )

            return bundlesResponse.map { bundle ->
                val firstTier = bundle.tiers.firstOrNull()
                val priceAmount = firstTier?.price?.amount ?: 0.0
                val priceCurrency = firstTier?.price?.currency ?: "USD"

                // try to get cover image from the first game in the first tier
                val coverImage = firstTier?.games?.firstOrNull()?.assets?.banner600
                    ?: firstTier?.games?.firstOrNull()?.assets?.boxart

                BundleInfo(
                    title = bundle.title,
                    store = bundle.page.name,
                    price = priceAmount,
                    currency = priceCurrency,
                    expiry = bundle.expiry,
                    link = bundle.url,
                    imageUrl = coverImage
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

}
