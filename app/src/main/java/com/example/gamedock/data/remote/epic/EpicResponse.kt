package com.example.gamedock.data.remote.epic

data class EpicResponse(
    val data: EpicData?
)

data class EpicData(
    val Catalog: EpicCatalog?
)

data class EpicCatalog(
    val searchStore: EpicSearchStore?
)

data class EpicSearchStore(
    val elements: List<EpicGameElement>?
)

data class EpicGameElement(
    val id: String?,
    val title: String?,
    val productSlug: String?,
    val keyImages: List<EpicKeyImage>?,
    val urlSlug: String?,
    val catalogNs: EpicCatalogNs?,
    val promotions: EpicPromotions,
    val price: EpicPrice?
)

data class EpicCatalogNs(
    val mappings: List<EpicMapping>?
)

data class EpicMapping(
    val pageSlug: String?,
    val pageType: String?
)

data class EpicKeyImage(
    val type: String?,
    val url: String?
)

data class EpicPromotions(
    val promotionalOffers: List<EpicPromotionWrapper>?,
    val upcomingPromotionalOffers: List<EpicPromotionWrapper>?,
)

data class EpicPromotionWrapper(
    val promotionalOffers: List<EpicPromotion>?
)

data class EpicPromotion(
    val startDate: String?,
    val endDate: String?,
    val discountSetting: EpicDiscountSetting?
)

data class EpicDiscountSetting(
    val discountType: String?,
    val discountPercentage: Int?
)

data class EpicPrice(
    val totalPrice: EpicTotalPrice?
)

data class EpicTotalPrice(
    val discount: Int?,
    val originalPrice: Int?,
    val finalPrice: Int?,
    val discountPrice: Int?,
    val discountPercentage: Int?
)

