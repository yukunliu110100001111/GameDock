package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Game
import com.example.gamedock.data.model.Offer

/**
 * Contract for retrieving deals-related information.
 */
interface DealsRepository {
    suspend fun getFreebies(): List<Game>
    suspend fun comparePrices(query: String): List<Offer>
}
