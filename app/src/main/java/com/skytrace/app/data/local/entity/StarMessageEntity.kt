package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "star_messages")
data class StarMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "target_name") val targetName: String,
    @ColumnInfo(name = "target_catalog_id") val targetCatalogId: String? = null,
    @ColumnInfo(name = "target_type") val targetType: String,
    @ColumnInfo(name = "target_ra") val targetRA: Double,
    @ColumnInfo(name = "target_dec") val targetDec: Double,
    @ColumnInfo(name = "distance_ly") val distanceLightYears: Double? = null,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "sender_name") val senderName: String,
    @ColumnInfo(name = "recipient_name") val recipientName: String? = null,
    @ColumnInfo(name = "transmission_time") val transmissionTime: Long,
    @ColumnInfo(name = "frequency") val frequency: Double = 1420.405,
    @ColumnInfo(name = "estimated_arrival_years") val estimatedArrivalYears: Double? = null,
    @ColumnInfo(name = "status") val status: String = "QUEUED",
    @ColumnInfo(name = "certificate_id") val certificateId: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
