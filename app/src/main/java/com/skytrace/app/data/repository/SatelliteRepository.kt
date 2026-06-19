package com.skytrace.app.data.repository

import com.skytrace.app.data.local.dao.CatalogCacheDao
import com.skytrace.app.data.local.dao.SatelliteTleDao
import com.skytrace.app.data.local.entity.CatalogCacheEntity
import com.skytrace.app.data.local.entity.SatelliteTleEntity
import com.skytrace.app.data.remote.api.CelesTrakApi
import com.skytrace.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SatelliteRepository @Inject constructor(
    private val celestrakApi: CelesTrakApi,
    private val satelliteTleDao: SatelliteTleDao,
    private val catalogCacheDao: CatalogCacheDao
) {

    fun getAllSatellites(): Flow<List<TLEData>> =
        satelliteTleDao.getAll().map { list -> list.map { it.toDomain() } }

    fun getByCategory(category: SatelliteCategory): Flow<List<TLEData>> =
        satelliteTleDao.getByCategory(category.name).map { list -> list.map { it.toDomain() } }

    fun searchSatellites(query: String): Flow<List<TLEData>> =
        satelliteTleDao.search(query).map { list -> list.map { it.toDomain() } }

    suspend fun getByNoradId(noradId: Int): TLEData? =
        satelliteTleDao.getByNoradId(noradId)?.toDomain()

    /**
     * Sync satellite TLE data from CelesTrak for a given category.
     */
    suspend fun syncCategory(category: SatelliteCategory): Result<Int> {
        return try {
            catalogCacheDao.insert(
                CatalogCacheEntity(
                    catalogName = "tle_${category.name}",
                    state = SyncState.SYNCING.name
                )
            )

            val response = celestrakApi.getTLEByGroup(category.celestrakGroup)
            if (!response.isSuccessful) {
                val error = "HTTP ${response.code()}: ${response.message()}"
                catalogCacheDao.updateError("tle_${category.name}", SyncState.FAILED.name, error)
                return Result.failure(Exception(error))
            }

            val body = response.body() ?: return Result.failure(Exception("Empty response"))
            val satellites = parseTleData(body, category)

            satelliteTleDao.deleteByCategory(category.name)
            satelliteTleDao.insertAll(satellites.map { it.toEntity(category) })

            catalogCacheDao.updateSyncState(
                "tle_${category.name}",
                System.currentTimeMillis(),
                satellites.size,
                SyncState.SYNCED.name
            )

            Result.success(satellites.size)
        } catch (e: Exception) {
            catalogCacheDao.updateError("tle_${category.name}", SyncState.FAILED.name, e.message)
            Result.failure(e)
        }
    }

    /**
     * Parse TLE text format (3-line format: name, line1, line2).
     */
    private fun parseTleData(raw: String, category: SatelliteCategory): List<TLEData> {
        val lines = raw.lines().filter { it.isNotBlank() }
        val satellites = mutableListOf<TLEData>()

        var i = 0
        while (i + 2 < lines.size) {
            val name = lines[i].trim()
            val line1 = lines[i + 1].trim()
            val line2 = lines[i + 2].trim()

            if (line1.startsWith("1 ") && line2.startsWith("2 ")) {
                val noradId = line1.substring(2, 7).trim().toIntOrNull() ?: 0
                satellites.add(
                    TLEData(
                        name = name,
                        noradId = noradId,
                        line1 = line1,
                        line2 = line2,
                        category = category
                    )
                )
                i += 3
            } else {
                i++
            }
        }
        return satellites
    }

    suspend fun getLastUpdateTime(category: SatelliteCategory): Long? =
        satelliteTleDao.getLastUpdateTime(category.name)

    suspend fun getCount(): Int = satelliteTleDao.getCount()

    private fun SatelliteTleEntity.toDomain(): TLEData = TLEData(
        name = name,
        noradId = noradId,
        line1 = line1,
        line2 = line2,
        category = try { SatelliteCategory.valueOf(category) } catch (e: Exception) { SatelliteCategory.OTHER }
    )

    private fun TLEData.toEntity(category: SatelliteCategory): SatelliteTleEntity = SatelliteTleEntity(
        noradId = noradId,
        name = name,
        line1 = line1,
        line2 = line2,
        category = category.name
    )
}
