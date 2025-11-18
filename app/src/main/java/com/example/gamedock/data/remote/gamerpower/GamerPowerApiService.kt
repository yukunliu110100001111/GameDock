package com.example.gamedock.data.remote.gamerpower

import com.example.gamedock.data.remote.dto.GamerPowerDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GamerPowerApiService {
    @GET("giveaways")
    suspend fun getGiveaways(
        @Query("type") type : String = "game"
    ) : List<GamerPowerDto>

}
