package com.example.gamedock.data.remote.epic

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EpicApiClient {
    private const val BASE_URL = "https://store-site-backend-static.ak.epicgames.com/"

    val api: EpicApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EpicApiService::class.java)
    }
}