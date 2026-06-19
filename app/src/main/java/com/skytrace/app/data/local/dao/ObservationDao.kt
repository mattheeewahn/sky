package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(observation: ObservationEntity): Long

    @Update
    suspend fun update(observation: ObservationEntity)

    @Delete
    suspend fun delete(observation: ObservationEntity)

    @Query("SELECT * FROM observations ORDER BY date_time DESC")
    fun getAllObservations(): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE id = :id")
    suspend fun getById(id: Long): ObservationEntity?

    @Query("SELECT * FROM observations WHERE object_name LIKE '%' || :query || '%' OR catalog_id LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE object_type = :type ORDER BY date_time DESC")
    fun getByType(type: String): Flow<List<ObservationEntity>>

    @Query("SELECT COUNT(*) FROM observations")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT object_name FROM observations ORDER BY date_time DESC LIMIT :limit")
    suspend fun getRecentObjectNames(limit: Int = 10): List<String>
}
