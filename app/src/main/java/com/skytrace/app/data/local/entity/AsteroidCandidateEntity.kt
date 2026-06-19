package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asteroid_candidates")
data class AsteroidCandidateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "observation_time") val observationTime: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "telescope") val telescope: String? = null,
    @ColumnInfo(name = "camera") val camera: String? = null,
    @ColumnInfo(name = "exposure_seconds") val exposureSeconds: Double? = null,
    @ColumnInfo(name = "field_of_view_arcmin") val fieldOfViewArcmin: Double? = null,
    @ColumnInfo(name = "center_ra") val centerRA: Double? = null,
    @ColumnInfo(name = "center_dec") val centerDec: Double? = null,
    @ColumnInfo(name = "plate_scale") val plateScale: Double? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "image_uris") val imageUris: String? = null, // JSON array
    @ColumnInfo(name = "status") val status: String = "DRAFT",
    @ColumnInfo(name = "verification_result") val verificationResult: String? = null, // JSON
    @ColumnInfo(name = "marked_x") val markedPositionX: Float? = null,
    @ColumnInfo(name = "marked_y") val markedPositionY: Float? = null,
    @ColumnInfo(name = "movement_deg_per_hour") val movementDegPerHour: Double? = null,
    @ColumnInfo(name = "movement_direction_deg") val movementDirectionDeg: Double? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
