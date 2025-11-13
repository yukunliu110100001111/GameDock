package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.remote.epic.EpicStoreAdapter
import com.example.gamedock.data.remote.itad.ItadApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealsRepositoryImpl @Inject constructor(
    private val epicStoreAdapter: EpicStoreAdapter,
    private val itadApi: ItadApiService
) : DealsRepository {

    /**
     * Fetches a list of freebies from the Epic Store.
     */
    override suspend fun getFreebies(): List<Freebie> {
        return epicStoreAdapter.fetchFreebies()
    }

    override suspend fun comparePrices(query: String): List<Offer> {
        // todo
        return emptyList()
    }
}
