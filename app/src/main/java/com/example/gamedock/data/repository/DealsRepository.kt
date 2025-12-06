package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.model.BundleDeal
import com.example.gamedock.data.remote.itad.ItadSearchItem

/**
 * Contract for retrieving deals-related information.
 */
interface DealsRepository {
    fun getCachedFreebies(): List<Freebie>
    suspend fun getFreebies(): List<Freebie>
    suspend fun searchGames(query: String): List<ItadSearchItem>
    suspend fun comparePrices(query: String): List<Offer>
    suspend fun comparePricesById(game: ItadSearchItem): List<Offer>
    suspend fun searchBundles(query: String): List<BundleDeal>
    suspend fun getBundlesFeed(): List<BundleDeal>
}
