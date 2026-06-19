package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.CatalogCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: CatalogCacheEntity)

    @Query("SELECT * FROM catalog_cache WHERE catalog_name = :name LIMIT 1")
    suspend fun getByCatalogName(name: String): CatalogCacheEntity?

    @Query("SELECT * FROM catalog_cache ORDER BY last_sync_time DESC")
    fun getAll(): Flow<List<CatalogCacheEntity>>

    @Query("UPDATE catalog_cache SET last_sync_time = :time, object_count = :count, state = :state WHERE catalog_name = :name")
    suspend fun updateSyncState(name: String, time: Long, count: Int, state: String)

    @Query("UPDATE catalog_cache SET state = :state, error_message = :error WHERE catalog_name = :name")
    suspend fun updateError(name: String, state: String, error: String?)
}
