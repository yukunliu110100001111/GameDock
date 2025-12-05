package com.example.gamedock.data.remote.itad

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.gamedock.data.model.BundleDeal
import com.example.gamedock.data.model.BundleGame
import com.example.gamedock.data.model.Offer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.time.Instant
import java.time.format.DateTimeParseException

@Singleton
class ItadAdapter @Inject constructor(
    private val itadApiService: ItadApiService,
    @ApplicationContext private val context: Context // 注入应用 Context 以读取当前语言/区域
) {

    private val ITAD_API_KEY = "d719761d720142c5f15a7b7b7177704783ffc227"

    /**
     * Resolves the country code based on the device's primary locale.
     * Defaults to "US" if the country code is unavailable or unsupported.
     */
    private fun resolveCountryCode(): String {
        val primaryLocale = context.resources.configuration.locales.get(0)
        val raw = primaryLocale.country.uppercase()
        if (raw.isBlank()) return "US"
        // Validate against supported country codes
        val supported = setOf(
            "US","CA","GB","DE","FR","AU","CN","JP","KR","BR","RU","ES","IT"
        )
        return if (supported.contains(raw)) raw else "US"
    }

    /**
     * Compares prices for a game based on the search query.
     */
    suspend fun comparePrices(gameQuery: String): List<Offer> {
        try {
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )

            val searchItem = searchResults.firstOrNull()
            val gameId = searchItem?.id
            val gameTitle = searchItem?.title

            val imageUrl = searchItem?.assets?.let { assets ->
                listOf(
                    assets.banner600,
                    assets.banner400,
                    assets.banner300,
                    assets.banner145,
                    assets.boxart
                ).firstOrNull { !it.isNullOrBlank() }
            }

            if (gameId == null || gameTitle == null) {
                Log.w("ItadAdapter", "No game found for query: $gameQuery")
                return emptyList()
            }

            val priceResponse = itadApiService.getGamePrices(
                apiKey = ITAD_API_KEY,
                country = resolveCountryCode(),
                gameIds = listOf(gameId),
                onlyDeals = null,
                capacity = null
            )

            val gameDetails = priceResponse.firstOrNull() ?: return emptyList()

            return gameDetails.deals.map { deal ->
                val storeLowestPrice = deal.storeLow?.amount ?: deal.price.amount
                Offer(
                    id = gameId,
                    gameTitle = gameTitle,
                    store = deal.shop.name,
                    currentPrice = deal.price.amount,
                    lowestPrice = storeLowestPrice,
                    currencyCode = deal.price.currency,
                    url = deal.url,
                    imageUrl = imageUrl
                )
            }
        } catch (_: Exception) {
            return emptyList()
        }
    }

    /**
     * Fetches the price overview for a game based on the search query.
     */
    suspend fun getPrice(gameQuery: String): List<Offer> {
        try {
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )
            val gameId = searchResults.firstOrNull()?.id ?: return emptyList()

            val priceResponse = itadApiService.getGamePriceOverview(
                apiKey = ITAD_API_KEY,
                country = resolveCountryCode(),
                gameIds = listOf(gameId)
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
                lowestPrice = lowestPriceDetails?.price?.amount ?: currentPriceDetails.price.amount,
                currencyCode = currentPriceDetails.price.currency,
                url = currentPriceDetails.url
            )
        } ?: emptyList()
    }

    /**
     * Searches for bundles containing the specified game.
     */
    suspend fun searchBundles(gameQuery: String): List<BundleDeal> {
        try {
            val searchResults = itadApiService.searchGame(
                apiKey = ITAD_API_KEY,
                title = gameQuery,
                resultCount = 1
            )
            val gameId = searchResults.firstOrNull()?.id ?: return emptyList()
            val gameTitle = searchResults.first().title

            val overview = itadApiService.getGamePriceOverview(
                apiKey = ITAD_API_KEY,
                country = resolveCountryCode(),
                gameIds = listOf(gameId)
            )

            val bundlesResponse = overview.bundles ?: return emptyList()

            return bundlesResponse
                .filter { isActiveBundle(it.expiry) }
                .map { bundle ->
                val firstTier = bundle.tiers.firstOrNull()
                val priceAmount = firstTier?.price?.amount ?: 0.0
                val priceCurrency = firstTier?.price?.currency ?: "USD"
                val coverImage = firstTier?.games?.firstOrNull()?.assets?.banner600
                    ?: firstTier?.games?.firstOrNull()?.assets?.boxart

                val games = bundle.tiers
                    .flatMap { it.games }
                    .distinctBy { it.title }
                    .map { game ->
                        BundleGame(
                            title = game.title,
                            imageUrl = game.assets?.banner145 ?: game.assets?.boxart
                        )
                    }

                BundleDeal(
                    id = bundle.id,
                    title = bundle.title,
                    store = bundle.page.name,
                    price = priceAmount,
                    currency = priceCurrency,
                    expiry = bundle.expiry,
                    link = bundle.url,
                    imageUrl = coverImage,
                    games = games
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    /**
     * Default feed: query a small set of popular games, then merge their overview bundles.
     */
    suspend fun getBundlesFeed(): List<BundleDeal> {
        val seedTitles = listOf(
            "cyberpunk 2077",
            "elden ring",
            "god of war",
            "rimworld",
            "hollow knight",
            "resident evil 4"
        )

        val gameIds = mutableListOf<String>()
        for (title in seedTitles) {
            val id = runCatching {
                itadApiService.searchGame(
                    apiKey = ITAD_API_KEY,
                    title = title,
                    resultCount = 1
                ).firstOrNull()?.id
            }.getOrNull()
            if (id != null) gameIds.add(id)
        }

        if (gameIds.isEmpty()) return emptyList()

        val overview = runCatching {
            itadApiService.getGamePriceOverview(
                apiKey = ITAD_API_KEY,
                country = resolveCountryCode(),
                gameIds = gameIds
            )
        }.getOrElse { return emptyList() }

        val bundles = overview.bundles?.filter { isActiveBundle(it.expiry) } ?: return emptyList()

        return bundles.map { bundle ->
            val firstTier = bundle.tiers.firstOrNull()
            val priceAmount = firstTier?.price?.amount ?: 0.0
            val priceCurrency = firstTier?.price?.currency ?: "USD"
            val coverImage = firstTier?.games?.firstOrNull()?.assets?.banner600
                ?: firstTier?.games?.firstOrNull()?.assets?.boxart

            val games = bundle.tiers
                .flatMap { it.games }
                .distinctBy { it.title }
                .map { game ->
                    BundleGame(
                        title = game.title,
                        imageUrl = game.assets?.banner145 ?: game.assets?.boxart
                    )
                }

            BundleDeal(
                id = bundle.id,
                title = bundle.title,
                store = bundle.page.name,
                price = priceAmount,
                currency = priceCurrency,
                expiry = bundle.expiry,
                link = bundle.url,
                imageUrl = coverImage,
                games = games
            )
        }
    }

    private fun isActiveBundle(expiry: String?): Boolean {
        val end = parseExpiryMillis(expiry)
        return end == null || end > System.currentTimeMillis()
    }

    private fun parseExpiryMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        return try {
            Instant.parse(raw).toEpochMilli()
        } catch (_: DateTimeParseException) {
            null
        }
    }

}
