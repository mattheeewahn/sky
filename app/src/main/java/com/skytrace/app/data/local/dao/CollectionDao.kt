package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CollectionEntity): Long

    @Update
    suspend fun update(entry: CollectionEntity)

    @Delete
    suspend fun delete(entry: CollectionEntity)

    @Query("SELECT * FROM collection ORDER BY last_observed DESC")
    fun getAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection WHERE object_type = :type")
    fun getByType(type: String): Flow<List<CollectionEntity>>

    @Query("SELECT COUNT(*) FROM collection WHERE object_type = :type")
    suspend fun getCountByType(type: String): Int

    @Query("SELECT * FROM collection WHERE object_name = :name OR catalog_id = :catalogId LIMIT 1")
    suspend fun findByNameOrCatalog(name: String, catalogId: String?): CollectionEntity?

    @Query("SELECT COUNT(*) FROM collection")
    suspend fun getTotalCount(): Int
}
