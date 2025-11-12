package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Game
import com.example.gamedock.data.model.Offer

/**
 * TODO: Wire up Retrofit + Room once data sources are ready.
 */
class DealsRepositoryImpl : DealsRepository {
    override suspend fun getFreebies(): List<Game> = emptyList()

    override suspend fun comparePrices(query: String): List<Offer> = emptyList()
}
