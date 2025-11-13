package com.example.gamedock.data.remote.epic

import androidx.compose.ui.text.intl.Locale
import retrofit2.http.GET
import retrofit2.http.Query

interface EpicApiService {
    @GET("freeGamesPromotions")
    suspend fun getFreeGames(
        @Query("locale") locale: String = "en-US",
        @Query("country") country: String = "US",
    ): EpicResponse
}