package com.tubes.nimons360.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteLocationDao {
    @Query("SELECT * FROM favourite_locations")
    fun getAll(): Flow<List<FavouriteLocationEntity>>

    @Insert
    suspend fun insert(location: FavouriteLocationEntity)

    @Query("DELETE FROM favourite_locations WHERE id = :id")
    suspend fun delete(id: Int)
}
