package com.example.gamedock.data.repo

import com.example.gamedock.domain.model.Game
import com.example.gamedock.domain.model.Offer

/**
 * Contract for retrieving deals-related information.
 */
interface DealsRepository {
    suspend fun getFreebies(): List<Game>
    suspend fun comparePrices(query: String): List<Offer>
}
