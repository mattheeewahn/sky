package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "satellite_tle")
data class SatelliteTleEntity(
    @PrimaryKey @ColumnInfo(name = "norad_id") val noradId: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "line1") val line1: String,
    @ColumnInfo(name = "line2") val line2: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)
