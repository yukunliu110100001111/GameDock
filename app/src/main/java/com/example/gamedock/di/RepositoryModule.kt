package com.example.gamedock.di

import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.repository.DealsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDealsRepository(
        dealsRepositoryImpl: DealsRepositoryImpl
    ): DealsRepository

//    fun bindDealsRepository(): DealsRepository {
//        val api = EpicApiClient.api
//        val adapter = EpicStoreAdapter(api)
//        return DealsRepositoryImpl(adapter)
//    }
}
