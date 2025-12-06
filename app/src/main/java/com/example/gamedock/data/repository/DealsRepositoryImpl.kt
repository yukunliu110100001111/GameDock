package com.example.gamedock.data.repository

import com.example.gamedock.data.model.Freebie
import com.example.gamedock.data.model.Offer
import com.example.gamedock.data.model.BundleDeal
import com.example.gamedock.data.local.FreebiesCache
import com.example.gamedock.data.remote.epic.EpicStoreAdapter
import com.example.gamedock.data.remote.gamerpower.GamerPowerStoreAdapter
import com.example.gamedock.data.remote.itad.ItadAdapter
import com.example.gamedock.data.remote.itad.ItadSearchItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DealsRepositoryImpl @Inject constructor(
    private val epicStoreAdapter: EpicStoreAdapter,
    private val itadAdapter: ItadAdapter,
    private val gamerPowerStoreAdapter: GamerPowerStoreAdapter,
    private val freebiesCache: FreebiesCache
) : DealsRepository {

    override fun getCachedFreebies(): List<Freebie> = freebiesCache.load()

    /**
     * Fetches a list of freebies from the Epic Store.
     */
    override suspend fun getFreebies(): List<Freebie> {
        // Try network sources first; fall back to cached list if they fail.
        val cached = freebiesCache.load()

        val network = runCatching {
            val epic = epicStoreAdapter.fetchFreebies()
            val other = gamerPowerStoreAdapter.fetchFreebies()
            epic + other
        }

        return network.onSuccess { freebiesCache.save(it) }
            .getOrElse { cached }
    }


    override suspend fun comparePrices(query: String): List<Offer> {
        return itadAdapter.comparePrices(query)
    }

    override suspend fun comparePricesById(game: ItadSearchItem): List<Offer> {
        return itadAdapter.comparePrices(gameId = game.id, gameTitle = game.title, imageUrl = game.assets.banner145)
    }

    override suspend fun searchBundles(query: String): List<BundleDeal> {
        return itadAdapter.searchBundles(query)
    }

    override suspend fun getBundlesFeed(): List<BundleDeal> {
        return itadAdapter.getBundlesFeed()
    }

    override suspend fun searchGames(query: String): List<ItadSearchItem> {
        return itadAdapter.searchGames(query)
    }
}
