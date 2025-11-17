package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.remote.epic.EpicStoreAdapter
import com.example.gamedock.data.remote.gamerpower.GamerPowerStoreAdapter
import com.example.gamedock.data.remote.itad.ItadAdapter
import com.example.gamedock.data.remote.itad.ItadApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealsRepositoryImpl @Inject constructor(
    private val epicStoreAdapter: EpicStoreAdapter,
    private val itadAdapter: ItadAdapter,
    private val gamerPowerStoreAdapter: GamerPowerStoreAdapter
) : DealsRepository {

    /**
     * Fetches a list of freebies from the Epic Store.
     */
    override suspend fun getFreebies(): List<Freebie> {
        val epic = epicStoreAdapter.fetchFreebies()
        val other = gamerPowerStoreAdapter.fetchFreebies()
        return epic + other
    }


    override suspend fun comparePrices(query: String): List<Offer> {
        return itadAdapter.comparePrices(query)
    }
}
