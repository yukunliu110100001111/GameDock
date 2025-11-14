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
    val price: Price, // 史低价格
    val regular: Price, // 当时的原价
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
    val game: String // 游戏在 ITAD 上的链接
)
