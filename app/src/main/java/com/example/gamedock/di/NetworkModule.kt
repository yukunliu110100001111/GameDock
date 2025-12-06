package com.example.gamedock.di

import com.example.gamedock.data.remote.epic.EpicApiService
import com.example.gamedock.data.remote.gamerpower.GamerPowerApiService
import com.example.gamedock.data.remote.itad.ItadApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing network-related dependencies such as Retrofit instances
 * and API service interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // -----------------------------------------------------------------
    // Base Components (Shared)
    // -----------------------------------------------------------------

    @Provides
    @Singleton
    fun provideGson(): Gson {
        // Shared Gson instance for Retrofit converters and JSON (watchlist, cache).
        return GsonBuilder().create()
    }

    // -----------------------------------------------------------------
    // Epic
    // -----------------------------------------------------------------

    private const val EPIC_BASE_URL = "https://store-site-backend-static.ak.epicgames.com/"

    @Provides
    @Singleton
    @Named("EpicRetrofit")
    fun provideEpicRetrofit(gson: Gson): Retrofit {
        // Retrofit client for Epic freebies API.
        return Retrofit.Builder()
            .baseUrl(EPIC_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideEpicApiService(@Named("EpicRetrofit") retrofit: Retrofit): EpicApiService {
        // Epic freebies service interface.
        return retrofit.create(EpicApiService::class.java)
    }

    // -----------------------------------------------------------------
    // Itad API
    // -----------------------------------------------------------------

    private const val ITAD_BASE_URL = "https://api.isthereanydeal.com/"

    @Provides
    @Singleton
    @Named("ItadRetrofit")
    fun provideItadRetrofit(gson: Gson): Retrofit {
        // Retrofit client for ITAD search/price APIs.
        return Retrofit.Builder()
            .baseUrl(ITAD_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideItadApiService(@Named("ItadRetrofit") retrofit: Retrofit): ItadApiService {
        // ITAD service interface.
        return retrofit.create(ItadApiService::class.java)
    }

    // -----------------------------------------------------------------
    // GamerPower API
    // -----------------------------------------------------------------

    private const val GAMERPOWER_BASE_URL = "https://www.gamerpower.com/api/"

    @Provides
    @Singleton
    @Named("GamerPowerRetrofit")
    fun provideGamerPowerRetrofit(gson: Gson): Retrofit {
        // Retrofit client for GamerPower giveaways feed.
        return Retrofit.Builder()
            .baseUrl(GAMERPOWER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGamerPowerApiService(
        @Named("GamerPowerRetrofit") retrofit: Retrofit
    ): GamerPowerApiService {
        // GamerPower service interface.
        return retrofit.create(GamerPowerApiService::class.java)
    }


}
