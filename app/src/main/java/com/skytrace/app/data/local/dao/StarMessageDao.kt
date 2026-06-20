package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.StarMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StarMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: StarMessageEntity): Long

    @Update
    suspend fun update(message: StarMessageEntity)

    @Query("SELECT * FROM star_messages ORDER BY created_at DESC")
    fun getAll(): Flow<List<StarMessageEntity>>

    @Query("SELECT * FROM star_messages WHERE id = :id")
    suspend fun getById(id: Long): StarMessageEntity?

    @Query("SELECT COUNT(*) FROM star_messages")
    suspend fun getCount(): Int

    @Query("UPDATE star_messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
