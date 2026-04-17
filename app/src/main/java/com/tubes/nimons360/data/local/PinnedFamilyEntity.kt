package com.tubes.nimons360.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pinned_families")
data class PinnedFamilyEntity(
    @PrimaryKey val familyId: Int,
    val name: String,
    val iconUrl: String
)

@Dao
interface PinnedFamilyDao {
    @Query("SELECT * FROM pinned_families")
    fun getAllPinned(): Flow<List<PinnedFamilyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun pin(family: PinnedFamilyEntity)

    @Query("DELETE FROM pinned_families WHERE familyId = :id")
    suspend fun unpin(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM pinned_families WHERE familyId = :id)")
    suspend fun isPinned(id: Int): Boolean
}