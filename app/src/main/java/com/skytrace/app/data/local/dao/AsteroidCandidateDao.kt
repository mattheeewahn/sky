package com.skytrace.app.data.local.dao

import androidx.room.*
import com.skytrace.app.data.local.entity.AsteroidCandidateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AsteroidCandidateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(candidate: AsteroidCandidateEntity): Long

    @Update
    suspend fun update(candidate: AsteroidCandidateEntity)

    @Delete
    suspend fun delete(candidate: AsteroidCandidateEntity)

    @Query("SELECT * FROM asteroid_candidates ORDER BY created_at DESC")
    fun getAll(): Flow<List<AsteroidCandidateEntity>>

    @Query("SELECT * FROM asteroid_candidates WHERE id = :id")
    suspend fun getById(id: Long): AsteroidCandidateEntity?

    @Query("SELECT * FROM asteroid_candidates WHERE status = :status ORDER BY created_at DESC")
    fun getByStatus(status: String): Flow<List<AsteroidCandidateEntity>>

    @Query("SELECT COUNT(*) FROM asteroid_candidates")
    suspend fun getCount(): Int
}
