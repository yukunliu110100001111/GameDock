package com.example.gamedock.domain.usecase

import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.domain.model.Offer

/**
 * Compares store prices for the supplied query.
 */
class ComparePricesUseCase(
    private val repository: DealsRepository
) {
    suspend operator fun invoke(query: String): List<Offer> = repository.comparePrices(query)
}
