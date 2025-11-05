package com.example.gamedock.di

import com.example.gamedock.data.repo.DealsRepository
import com.example.gamedock.data.repo.FakeDealsRepository

/**
 * Provides repository instances for the composables/ViewModels.
 */
object RepositoryModule {
    fun provideDealsRepository(): DealsRepository = FakeDealsRepository()
}
