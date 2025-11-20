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

// ------------------- Detailed Price Data ------------------

typealias GamePriceDetailsResponse = List<GamePriceDetails>

/**
 * 单个游戏详情，对应列表中的每个元素。
 */
data class GamePriceDetails(
    val id: String, // 游戏ID
    val historyLow: HistoryLow, // 历史低价
    val deals: List<Deal> // 当前所有优惠
)

/**
 * 3. 历史低价 (聚合)
 */
data class HistoryLow(
    val all: Price, // 全时段史低
    val y1: Price, // 1年内史低
    val m3: Price // 3个月内史低
)

/**
 * 4. 单个优惠详情
 * 对应 "deals" 数组中的每个元素。
 */
data class Deal(
    val shop: Shop,
    val price: Price, // 当前售价
    val regular: Price, // 原价
    val cut: Int, // 折扣
    val storeLow: Price?, // 本店史低
    val drm: List<Drm>,
    val url: String
)

// ------------------- Bundles ------------------

typealias GameBundlesResponse = List<BundleDetail>

data class BundleDetail(
    val id: Int,
    val title: String,
    val url: String, // 购买链接
    val page: BundlePage, // 包含商店信息
    val publish: String, // 发布时间
    val expiry: String?, // 过期时间 (可能为空)
    val tiers: List<BundleTier> // 价格层级
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
