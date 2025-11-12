package com.example.gamedock.di

import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.repository.FakeDealsRepository

/**
 * Provides repository instances for the composables/ViewModels.
 */
object RepositoryModule {
    fun provideDealsRepository(): DealsRepository = FakeDealsRepository()
}
