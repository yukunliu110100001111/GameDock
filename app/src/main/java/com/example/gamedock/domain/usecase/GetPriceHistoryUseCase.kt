package com.example.gamedock.domain.usecase

import com.example.gamedock.domain.model.PricePoint

/**
 * Placeholder use case for retrieving historical pricing.
 */
class GetPriceHistoryUseCase {
    suspend operator fun invoke(gameId: String): List<PricePoint> = emptyList()
}
