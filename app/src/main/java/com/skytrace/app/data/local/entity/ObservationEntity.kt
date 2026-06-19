package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "object_name") val objectName: String,
    @ColumnInfo(name = "catalog_id") val catalogId: String? = null,
    @ColumnInfo(name = "object_type") val objectType: String,
    @ColumnInfo(name = "date_time") val dateTime: Long,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "telescope") val telescope: String? = null,
    @ColumnInfo(name = "eyepiece") val eyepiece: String? = null,
    @ColumnInfo(name = "camera") val camera: String? = null,
    @ColumnInfo(name = "filter") val filter: String? = null,
    @ColumnInfo(name = "exposure_seconds") val exposureSeconds: Double? = null,
    @ColumnInfo(name = "seeing_condition") val seeingCondition: String? = null,
    @ColumnInfo(name = "transparency") val transparency: String? = null,
    @ColumnInfo(name = "sky_brightness") val skyBrightness: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "photo_uris") val photoUris: String? = null, // JSON array string
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis()
)
