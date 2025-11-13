package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.remote.epic.EpicStoreAdapter

class DealsRepositoryImpl(
    private val epicStoreAdapter: EpicStoreAdapter
) : DealsRepository {

    override suspend fun getFreebies(): List<Freebie> {
        return epicStoreAdapter.fetchFreebies()
    }

    override suspend fun comparePrices(query: String): List<Offer> {
        return emptyList()
    }
}
