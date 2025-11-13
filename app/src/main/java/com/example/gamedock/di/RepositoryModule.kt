package com.example.gamedock.di

import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.repository.DealsRepositoryImpl
import com.example.gamedock.data.remote.epic.EpicApiClient
import com.example.gamedock.data.remote.epic.EpicStoreAdapter

object RepositoryModule {
    fun provideDealsRepository(): DealsRepository {
        val api = EpicApiClient.api
        val adapter = EpicStoreAdapter(api)
        return DealsRepositoryImpl(adapter)
    }
}
