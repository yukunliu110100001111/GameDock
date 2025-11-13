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
    val promotions: EpicPromotions?
)

data class EpicPromotions(
    val promotionalOffers: List<EpicPromotionWrapper>?,
    val upcomingPromotionalOffers: List<EpicPromotionWrapper>?
)

data class EpicPromotionWrapper(
    val promotionalOffers: List<EpicPromotion>?
)

data class EpicPromotion(
    val startDate: String?,
    val endDate: String?
)

