package com.example.gamedock.data.local.watchlist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Query("SELECT * FROM watchlist")
    fun getAll(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE gameId = :id")
    suspend fun getById(id: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(watchlistEntity: WatchlistEntity)


    @Query("DELETE FROM watchlist WHERE gameId = :id")
    suspend fun deleteById(id: String)

    @Update
    suspend fun update(item: WatchlistEntity)
}
