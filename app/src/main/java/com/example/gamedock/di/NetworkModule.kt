package com.example.gamedock.di

import com.example.gamedock.data.remote.DealsApi

/**
 * Manual dependency provider that mimics what Hilt will supply later.
 */
object NetworkModule {
    fun provideDealsApi(): DealsApi = object : DealsApi {}
}
