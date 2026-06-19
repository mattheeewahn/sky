package com.skytrace.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_cache")
data class CatalogCacheEntity(
    @PrimaryKey @ColumnInfo(name = "catalog_name") val catalogName: String,
    @ColumnInfo(name = "last_sync_time") val lastSyncTime: Long? = null,
    @ColumnInfo(name = "object_count") val objectCount: Int = 0,
    @ColumnInfo(name = "state") val state: String = "NEVER_SYNCED",
    @ColumnInfo(name = "error_message") val errorMessage: String? = null
)
