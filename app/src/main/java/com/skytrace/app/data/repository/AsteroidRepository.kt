package com.skytrace.app.data.repository

import com.skytrace.app.data.local.dao.AsteroidCandidateDao
import com.skytrace.app.data.local.entity.AsteroidCandidateEntity
import com.skytrace.app.data.remote.api.JplApi
import com.skytrace.app.data.remote.api.MpcApi
import com.skytrace.app.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsteroidRepository @Inject constructor(
    private val mpcApi: MpcApi,
    private val jplApi: JplApi,
    private val candidateDao: AsteroidCandidateDao
) {
    private val gson = Gson()

    // --- Remote data access ---

    /**
     * Search MPC orbital database for known asteroids.
     */
    suspend fun searchMpc(query: String): Result<String> {
        return try {
            val response = mpcApi.searchOrbits(name = query)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("MPC search failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lookup asteroid/comet in JPL Small-Body Database.
     */
    suspend fun searchJpl(query: String): Result<String> {
        return try {
            val response = jplApi.lookupSmallBody(query)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("JPL lookup failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get JPL Horizons ephemeris for a body.
     */
    suspend fun getEphemeris(
        bodyId: String,
        location: ObserverLocation,
        startTime: String,
        stopTime: String
    ): Result<String> {
        return try {
            val siteCoord = "${location.longitude},${location.latitude},${location.altitudeMeters / 1000.0}"
            val response = jplApi.getEphemeris(
                command = "'$bodyId'",
                center = "coord@399",
                siteCoord = siteCoord,
                startTime = startTime,
                stopTime = stopTime
            )
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("Horizons query failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get close approach data from JPL.
     */
    suspend fun getCloseApproaches(dateMin: String? = null, dateMax: String? = null): Result<String> {
        return try {
            val response = jplApi.getCloseApproaches(dateMin = dateMin, dateMax = dateMax)
            if (response.isSuccessful) {
                Result.success(response.body() ?: "")
            } else {
                Result.failure(Exception("Close approach query failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Local candidate management ---

    fun getAllCandidates(): Flow<List<AsteroidCandidate>> =
        candidateDao.getAll().map { list -> list.map { it.toDomain() } }

    fun getCandidatesByStatus(status: CandidateStatus): Flow<List<AsteroidCandidate>> =
        candidateDao.getByStatus(status.name).map { list -> list.map { it.toDomain() } }

    suspend fun getCandidateById(id: Long): AsteroidCandidate? =
        candidateDao.getById(id)?.toDomain()

    suspend fun saveCandidate(candidate: AsteroidCandidate): Long =
        candidateDao.insert(candidate.toEntity())

    suspend fun updateCandidate(candidate: AsteroidCandidate) =
        candidateDao.update(candidate.toEntity())

    suspend fun deleteCandidate(candidate: AsteroidCandidate) =
        candidateDao.delete(candidate.toEntity())

    private fun AsteroidCandidateEntity.toDomain(): AsteroidCandidate = AsteroidCandidate(
        id = id,
        observationTime = observationTime,
        latitude = latitude,
        longitude = longitude,
        telescope = telescope,
        camera = camera,
        exposureSeconds = exposureSeconds,
        fieldOfViewArcmin = fieldOfViewArcmin,
        centerRA = centerRA,
        centerDec = centerDec,
        plateScaleArcsecPerPixel = plateScale,
        notes = notes,
        imageUris = imageUris?.let {
            gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: emptyList(),
        status = try { CandidateStatus.valueOf(status) } catch (e: Exception) { CandidateStatus.DRAFT },
        verificationResult = verificationResult?.let {
            try { gson.fromJson(it, VerificationResult::class.java) } catch (e: Exception) { null }
        },
        markedPositionX = markedPositionX,
        markedPositionY = markedPositionY,
        estimatedMovementDegPerHour = movementDegPerHour,
        movementDirectionDeg = movementDirectionDeg,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun AsteroidCandidate.toEntity(): AsteroidCandidateEntity = AsteroidCandidateEntity(
        id = id,
        observationTime = observationTime,
        latitude = latitude,
        longitude = longitude,
        telescope = telescope,
        camera = camera,
        exposureSeconds = exposureSeconds,
        fieldOfViewArcmin = fieldOfViewArcmin,
        centerRA = centerRA,
        centerDec = centerDec,
        plateScale = plateScaleArcsecPerPixel,
        notes = notes,
        imageUris = if (imageUris.isNotEmpty()) gson.toJson(imageUris) else null,
        status = status.name,
        verificationResult = verificationResult?.let { gson.toJson(it) },
        markedPositionX = markedPositionX,
        markedPositionY = markedPositionY,
        movementDegPerHour = estimatedMovementDegPerHour,
        movementDirectionDeg = movementDirectionDeg,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
