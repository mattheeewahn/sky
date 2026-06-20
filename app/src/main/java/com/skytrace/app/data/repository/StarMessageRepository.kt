package com.skytrace.app.data.repository

import com.skytrace.app.data.local.dao.StarMessageDao
import com.skytrace.app.data.local.entity.StarMessageEntity
import com.skytrace.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StarMessageRepository @Inject constructor(
    private val starMessageDao: StarMessageDao
) {

    fun getAllMessages(): Flow<List<StarMessage>> =
        starMessageDao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): StarMessage? =
        starMessageDao.getById(id)?.toDomain()

    suspend fun sendMessage(
        target: CelestialObject,
        message: String,
        senderName: String,
        recipientName: String?,
        tier: MessageTier
    ): StarMessage {
        val distanceLY = estimateDistanceLY(target)
        val arrivalYears = distanceLY

        val starMessage = StarMessage(
            targetName = target.name,
            targetCatalogId = target.catalogId,
            targetType = target.type,
            targetRA = target.rightAscension,
            targetDec = target.declination,
            distanceLightYears = distanceLY,
            message = message.take(tier.maxChars),
            senderName = senderName,
            recipientName = recipientName,
            transmissionTime = System.currentTimeMillis(),
            estimatedArrivalYears = arrivalYears,
            status = TransmissionStatus.QUEUED,
            certificateId = generateCertificateId()
        )

        val id = starMessageDao.insert(starMessage.toEntity())
        return starMessage.copy(id = id)
    }

    suspend fun updateStatus(id: Long, status: TransmissionStatus) {
        starMessageDao.updateStatus(id, status.name)
    }

    suspend fun getCount(): Int = starMessageDao.getCount()

    /**
     * Estimate distance in light-years based on object type.
     * Real distances would come from catalog data.
     */
    private fun estimateDistanceLY(target: CelestialObject): Double {
        return when (target.type) {
            ObjectType.MOON -> 0.0000040 // 1.3 light-seconds
            ObjectType.SUN -> 0.0000158 // 8.3 light-minutes
            ObjectType.PLANET -> when {
                target.name.contains("Mars", true) -> 0.0000038 // ~3-22 light-minutes
                target.name.contains("Venus", true) -> 0.0000024
                target.name.contains("Jupiter", true) -> 0.0000092
                target.name.contains("Saturn", true) -> 0.000015
                target.name.contains("Neptune", true) -> 0.00047
                else -> 0.00001
            }
            ObjectType.STAR -> target.distanceAU?.let { it / 63241.0 } ?: when {
                target.name.contains("Sirius", true) -> 8.6
                target.name.contains("Vega", true) -> 25.0
                target.name.contains("Betelgeuse", true) -> 700.0
                target.name.contains("Polaris", true) -> 433.0
                target.name.contains("Deneb", true) -> 2600.0
                else -> 100.0 // average estimate
            }
            ObjectType.MESSIER -> when {
                target.catalogId?.contains("M31") == true -> 2537000.0 // Andromeda
                target.catalogId?.contains("M42") == true -> 1344.0 // Orion Nebula
                target.catalogId?.contains("M45") == true -> 444.0 // Pleiades
                target.catalogId?.contains("M1") == true -> 6523.0 // Crab Nebula
                target.catalogId?.contains("M13") == true -> 25100.0 // Hercules Cluster
                target.catalogId?.contains("M87") == true -> 53490000.0 // Virgo A
                else -> 30000.0 // average Messier
            }
            ObjectType.NGC -> 50000.0 // rough average
            ObjectType.ASTEROID -> 0.000005
            ObjectType.SATELLITE -> 0.00000004 // LEO ~400km
            else -> 1000.0
        }
    }

    private fun generateCertificateId(): String {
        val uuid = UUID.randomUUID().toString().take(8).uppercase()
        return "ST-$uuid"
    }

    private fun StarMessageEntity.toDomain(): StarMessage = StarMessage(
        id = id,
        targetName = targetName,
        targetCatalogId = targetCatalogId,
        targetType = try { ObjectType.valueOf(targetType) } catch (e: Exception) { ObjectType.STAR },
        targetRA = targetRA,
        targetDec = targetDec,
        distanceLightYears = distanceLightYears,
        message = message,
        senderName = senderName,
        recipientName = recipientName,
        transmissionTime = transmissionTime,
        frequency = frequency,
        estimatedArrivalYears = estimatedArrivalYears,
        status = try { TransmissionStatus.valueOf(status) } catch (e: Exception) { TransmissionStatus.QUEUED },
        certificateId = certificateId,
        createdAt = createdAt
    )

    private fun StarMessage.toEntity(): StarMessageEntity = StarMessageEntity(
        id = id,
        targetName = targetName,
        targetCatalogId = targetCatalogId,
        targetType = targetType.name,
        targetRA = targetRA,
        targetDec = targetDec,
        distanceLightYears = distanceLightYears,
        message = message,
        senderName = senderName,
        recipientName = recipientName,
        transmissionTime = transmissionTime,
        frequency = frequency,
        estimatedArrivalYears = estimatedArrivalYears,
        status = status.name,
        certificateId = certificateId,
        createdAt = createdAt
    )
}
