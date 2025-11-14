package com.example.gamedock.data.remote.itad

import com.example.gamedock.data.model.Offer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItadAdapter @Inject constructor(
    private val itadApiService: ItadApiService
) {

    private val ITAD_API_KEY = "d719761d720142c5f15a7b7b7177704783ffc227"

    suspend fun comparePrices(gameQuery: String): List<Offer> {
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
            // todo: country code should be dynamic
            val countryCode = "US"

            val priceResponse = itadApiService.getGamePrices(
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

}