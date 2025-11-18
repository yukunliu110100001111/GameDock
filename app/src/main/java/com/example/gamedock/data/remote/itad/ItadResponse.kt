package com.example.gamedock.data.remote.itad


// ------------------- Search API ------------------

/**
 * Response DTO for the ITAD Search API.
 * The API returns a list; this represents a single item in that list.
 */
data class ItadSearchItem(
    val id: String,
    val slug: String,
    val title: String,
    val type: String,
    val mature: Boolean,
    val assets: ItadAssets
)

/**
 * Image assets for a game referenced by an ItadSearchItem.
 */
data class ItadAssets(
    val banner145: String?,
    val banner300: String?,
    val banner400: String?,
    val banner600: String?,
    val boxart: String?
)

// ------------------- Price Overview ------------------

data class GamePriceResponse(
    val prices: List<GamePriceInfo>
)

data class GamePriceInfo(
    val id: String,
    val current: PriceDetails?,
    val lowest: LowestPrice?,
    val bundled: Int,
    val urls: GameUrls
)

data class PriceDetails(
    val shop: Shop,
    val price: Price, // current price
    val regular: Price, // original price
    val drm: List<Drm>,
    val url: String
)


data class LowestPrice(
    val shop: Shop,
    val price: Price, // historical low price
    val regular: Price, // original price when the low was recorded
)

data class Shop(
    val id: Int,
    val name: String
)

data class Price(
    val amount: Double,
    val amountInt: Int,
    val currency: String
)

data class Drm(
    val id: Int,
    val name: String
)

data class GameUrls(
    val game: String // link to the game page on ITAD
)

// ------------------- Detailed Price Data ------------------

typealias GamePriceDetailsResponse = List<GamePriceDetails>

/**
 * Detailed info for a single game (each entry in the list).
 */
data class GamePriceDetails(
    val id: String, // game identifier
    val historyLow: HistoryLow, // aggregated price history
    val deals: List<Deal> // current deals
)

/**
 * Aggregated historical lows.
 */
data class HistoryLow(
    val all: Price, // all-time low
    val y1: Price, // low within the last year
    val m3: Price // low within the last three months
)

/**
 * Single deal details (each entry in the `deals` list).
 */
data class Deal(
    val shop: Shop,
    val price: Price, // current price
    val regular: Price, // original price
    val cut: Int, // discount percent
    val storeLow: Price?, // lowest price on this store
    val drm: List<Drm>,
    val url: String
)


