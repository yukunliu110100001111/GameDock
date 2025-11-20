package com.example.gamedock.data.repository

import com.example.gamedock.data.model.BundleInfo
import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer

/**
 * Contract for retrieving deals-related information.
 */
interface DealsRepository {
    suspend fun getFreebies(): List<Freebie>
    suspend fun comparePrices(query: String): List<Offer>
    suspend fun searchBundles(query: String): List<BundleInfo>
}
