package com.skytrace.app.data.repository

import com.skytrace.app.data.local.dao.CollectionDao
import com.skytrace.app.data.local.entity.CollectionEntity
import com.skytrace.app.domain.model.CollectionEntry
import com.skytrace.app.domain.model.CollectionSummary
import com.skytrace.app.domain.model.ObjectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao
) {

    fun getAll(): Flow<List<CollectionEntry>> =
        collectionDao.getAll().map { list -> list.map { it.toDomain() } }

    fun getByType(type: ObjectType): Flow<List<CollectionEntry>> =
        collectionDao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    suspend fun addOrUpdate(objectName: String, catalogId: String?, objectType: ObjectType) {
        val existing = collectionDao.findByNameOrCatalog(objectName, catalogId)
        if (existing != null) {
            collectionDao.update(
                existing.copy(
                    lastObserved = System.currentTimeMillis(),
                    observationCount = existing.observationCount + 1
                )
            )
        } else {
            collectionDao.insert(
                CollectionEntity(
                    objectName = objectName,
                    catalogId = catalogId,
                    objectType = objectType.name,
                    firstObserved = System.currentTimeMillis(),
                    lastObserved = System.currentTimeMillis(),
                    observationCount = 1
                )
            )
        }
    }

    suspend fun getSummary(): CollectionSummary {
        return CollectionSummary(
            observedPlanets = collectionDao.getCountByType(ObjectType.PLANET.name),
            observedMessier = collectionDao.getCountByType(ObjectType.MESSIER.name),
            observedNGC = collectionDao.getCountByType(ObjectType.NGC.name),
            observedAsteroids = collectionDao.getCountByType(ObjectType.ASTEROID.name),
            observedComets = collectionDao.getCountByType(ObjectType.COMET.name),
            observedSatellites = collectionDao.getCountByType(ObjectType.SATELLITE.name),
            moonObserved = collectionDao.getCountByType(ObjectType.MOON.name) > 0
        )
    }

    suspend fun delete(entry: CollectionEntry) {
        collectionDao.delete(entry.toEntity())
    }

    private fun CollectionEntity.toDomain(): CollectionEntry = CollectionEntry(
        id = id,
        objectName = objectName,
        catalogId = catalogId,
        objectType = try { ObjectType.valueOf(objectType) } catch (e: Exception) { ObjectType.UNKNOWN },
        firstObserved = firstObserved,
        lastObserved = lastObserved,
        observationCount = observationCount,
        notes = notes
    )

    private fun CollectionEntry.toEntity(): CollectionEntity = CollectionEntity(
        id = id,
        objectName = objectName,
        catalogId = catalogId,
        objectType = objectType.name,
        firstObserved = firstObserved,
        lastObserved = lastObserved,
        observationCount = observationCount,
        notes = notes
    )
}
