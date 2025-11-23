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
    val regular: Price, // original list price
    val drm: List<Drm>,
    val url: String
)


data class LowestPrice(
    val shop: Shop,
    val price: Price, // historical lowest price
    val regular: Price, // original price at that moment
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
    val game: String // game page URL on ITAD
)

// ------------------- Detailed Price Data ------------------

typealias GamePriceDetailsResponse = List<GamePriceDetails>

/**
 * Single game price details entry.
 */
data class GamePriceDetails(
    val id: String, // game ID
    val historyLow: HistoryLow, // aggregated historical lowest prices
    val deals: List<Deal> // all current deals
)

/**
 * Aggregated historical low pricing.
 */
data class HistoryLow(
    val all: Price, // all-time low
    val y1: Price, // 1-year low
    val m3: Price // 3-month low
)

/**
 * Single deal entry within the deals list.
 */
data class Deal(
    val shop: Shop,
    val price: Price, // current price
    val regular: Price, // original price
    val cut: Int, // discount percentage
    val storeLow: Price?, // store-specific historical low
    val drm: List<Drm>,
    val url: String
)

// ------------------- Bundles ------------------

typealias GameBundlesResponse = List<BundleDetail>

data class BundleDetail(
    val id: Int,
    val title: String,
    val url: String, // purchase link
    val page: BundlePage, // store/page info
    val publish: String, // publish time
    val expiry: String?, // expiry time (nullable)
    val tiers: List<BundleTier> // pricing tiers
)

data class BundlePage(
    val name: String
)

data class BundleTier(
    val price: Price,
    val games: List<BundleGameItem>
)

data class BundleGameItem(
    val title: String,
    val assets: ItadAssets? = null
)
