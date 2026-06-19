package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collection")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "object_name") val objectName: String,
    @ColumnInfo(name = "catalog_id") val catalogId: String? = null,
    @ColumnInfo(name = "object_type") val objectType: String,
    @ColumnInfo(name = "first_observed") val firstObserved: Long,
    @ColumnInfo(name = "last_observed") val lastObserved: Long,
    @ColumnInfo(name = "observation_count") val observationCount: Int = 1,
    @ColumnInfo(name = "notes") val notes: String? = null
)
