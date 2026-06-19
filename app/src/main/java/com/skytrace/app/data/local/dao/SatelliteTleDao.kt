package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.SatelliteTleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SatelliteTleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(satellites: List<SatelliteTleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(satellite: SatelliteTleEntity)

    @Query("SELECT * FROM satellite_tle ORDER BY name ASC")
    fun getAll(): Flow<List<SatelliteTleEntity>>

    @Query("SELECT * FROM satellite_tle WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<SatelliteTleEntity>>

    @Query("SELECT * FROM satellite_tle WHERE norad_id = :noradId LIMIT 1")
    suspend fun getByNoradId(noradId: Int): SatelliteTleEntity?

    @Query("SELECT * FROM satellite_tle WHERE name LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<SatelliteTleEntity>>

    @Query("SELECT COUNT(*) FROM satellite_tle")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM satellite_tle WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int

    @Query("DELETE FROM satellite_tle WHERE category = :category")
    suspend fun deleteByCategory(category: String)

    @Query("SELECT MAX(last_updated) FROM satellite_tle WHERE category = :category")
    suspend fun getLastUpdateTime(category: String): Long?
}
