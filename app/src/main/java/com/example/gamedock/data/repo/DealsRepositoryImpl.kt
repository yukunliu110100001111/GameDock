package com.example.gamedock.data.repo

import com.example.gamedock.domain.model.Game
import com.example.gamedock.domain.model.Offer

/**
 * TODO: Wire up Retrofit + Room once data sources are ready.
 */
class DealsRepositoryImpl : DealsRepository {
    override suspend fun getFreebies(): List<Game> = emptyList()

    override suspend fun comparePrices(query: String): List<Offer> = emptyList()
}
