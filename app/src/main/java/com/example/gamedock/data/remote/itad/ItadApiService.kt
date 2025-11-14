package com.example.gamedock.data.remote.itad

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ItadApiService {

    /**
     * Search games via the ITAD API.
     * @param apiKey API key.
     * @param title Search keyword entered by the user.
     * @param resultCount Max number of results to return (1–100, default 20).
     */
    @GET("games/search/v1")
    suspend fun searchGame(
        @Query("key") apiKey: String,
        @Query("title") title: String,
        @Query("result") resultCount: Int
    ): List<ItadSearchItem>

    /**
     * Get price overview for a list of game IDs.
     * @param apiKey API key.
     * @param country Country code for localized pricing.
     * @param shopIds Optional list of shop IDs to filter results.
     * @param allowVouchers Whether to include voucher prices.
     * @param gameIds List of game IDs to query.
     */
    @POST("games/overview/v2")
    suspend fun getGamePrices(
        @Query("key") apiKey: String,
        @Query("country") country: String,
        @Query("shops") shopIds: List<Int>? = null,
        @Query("vouchers") allowVouchers: Boolean? = null,

        @Body gameIds: List<String>
    ): GamePriceResponse
}