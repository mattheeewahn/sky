package com.skytrace.app.data.repository

import com.skytrace.app.data.local.dao.ObservationDao
import com.skytrace.app.data.local.entity.ObservationEntity
import com.skytrace.app.domain.model.Observation
import com.skytrace.app.domain.model.ObjectType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservationRepository @Inject constructor(
    private val observationDao: ObservationDao
) {
    private val gson = Gson()

    fun getAllObservations(): Flow<List<Observation>> =
        observationDao.getAllObservations().map { list -> list.map { it.toDomain() } }

    fun searchObservations(query: String): Flow<List<Observation>> =
        observationDao.search(query).map { list -> list.map { it.toDomain() } }

    fun getByType(type: ObjectType): Flow<List<Observation>> =
        observationDao.getByType(type.name).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Observation? =
        observationDao.getById(id)?.toDomain()

    suspend fun save(observation: Observation): Long {
        val entity = observation.toEntity()
        return observationDao.insert(entity)
    }

    suspend fun update(observation: Observation) {
        observationDao.update(observation.toEntity())
    }

    suspend fun delete(observation: Observation) {
        observationDao.delete(observation.toEntity())
    }

    suspend fun getCount(): Int = observationDao.getCount()

    fun exportToJson(): Flow<String> = getAllObservations().map { observations ->
        gson.toJson(observations)
    }

    fun exportToCsv(): Flow<String> = getAllObservations().map { observations ->
        val header = "Object Name,Catalog ID,Type,Date/Time,Latitude,Longitude,Telescope,Eyepiece,Camera,Filter,Exposure,Seeing,Transparency,Sky Brightness,Notes\n"
        val rows = observations.joinToString("\n") { obs ->
            "${obs.objectName},${obs.catalogId ?: ""},${obs.objectType},${obs.dateTime},${obs.latitude},${obs.longitude},${obs.telescope ?: ""},${obs.eyepiece ?: ""},${obs.camera ?: ""},${obs.filter ?: ""},${obs.exposureSeconds ?: ""},${obs.seeingCondition?.label ?: ""},${obs.transparency?.label ?: ""},${obs.skyBrightness?.label ?: ""},\"${obs.notes ?: ""}\""
        }
        header + rows
    }

    private fun ObservationEntity.toDomain(): Observation = Observation(
        id = id,
        objectName = objectName,
        catalogId = catalogId,
        objectType = try { ObjectType.valueOf(objectType) } catch (e: Exception) { ObjectType.UNKNOWN },
        dateTime = dateTime,
        latitude = latitude,
        longitude = longitude,
        telescope = telescope,
        eyepiece = eyepiece,
        camera = camera,
        filter = filter,
        exposureSeconds = exposureSeconds,
        seeingCondition = seeingCondition?.let {
            try { com.skytrace.app.domain.model.SeeingCondition.valueOf(it) } catch (e: Exception) { null }
        },
        transparency = transparency?.let {
            try { com.skytrace.app.domain.model.Transparency.valueOf(it) } catch (e: Exception) { null }
        },
        skyBrightness = skyBrightness?.let {
            try { com.skytrace.app.domain.model.SkyBrightness.valueOf(it) } catch (e: Exception) { null }
        },
        notes = notes,
        photoUris = photoUris?.let {
            gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Observation.toEntity(): ObservationEntity = ObservationEntity(
        id = id,
        objectName = objectName,
        catalogId = catalogId,
        objectType = objectType.name,
        dateTime = dateTime,
        latitude = latitude,
        longitude = longitude,
        telescope = telescope,
        eyepiece = eyepiece,
        camera = camera,
        filter = filter,
        exposureSeconds = exposureSeconds,
        seeingCondition = seeingCondition?.name,
        transparency = transparency?.name,
        skyBrightness = skyBrightness?.name,
        notes = notes,
        photoUris = if (photoUris.isNotEmpty()) gson.toJson(photoUris) else null,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
