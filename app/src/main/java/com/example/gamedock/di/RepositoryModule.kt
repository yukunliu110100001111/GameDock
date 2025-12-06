package com.example.gamedock.di

import com.example.gamedock.data.repository.AccountCredentialsProvider
import com.example.gamedock.data.repository.AccountCredentialsProviderImpl
import com.example.gamedock.data.repository.AccountsRepository
import com.example.gamedock.data.repository.AccountsRepositoryImpl
import com.example.gamedock.data.repository.DealsRepository
import com.example.gamedock.data.repository.DealsRepositoryImpl
import com.example.gamedock.data.repository.EpicAuthRepository
import com.example.gamedock.data.repository.EpicAuthRepositoryImpl
import com.example.gamedock.data.watchlist.WatchlistRepository
import com.example.gamedock.data.watchlist.WatchlistRepositoryImpl
import com.example.gamedock.data.repository.SettingsRepository
import com.example.gamedock.data.repository.SettingsRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindAccountsRepository(
        impl: AccountsRepositoryImpl
    ): AccountsRepository

    @Binds
    @Singleton
    abstract fun bindEpicAuthRepository(
        impl: EpicAuthRepositoryImpl
    ): EpicAuthRepository

    @Binds
    @Singleton
    abstract fun bindAccountCredentialsProvider(
        impl: AccountCredentialsProviderImpl
    ): AccountCredentialsProvider

    @Binds
    @Singleton
    abstract fun bindWatchlistRepository(
        impl: WatchlistRepositoryImpl
    ): WatchlistRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}
