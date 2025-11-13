package com.example.gamedock.data.remote.itad

import retrofit2.http.GET
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
}