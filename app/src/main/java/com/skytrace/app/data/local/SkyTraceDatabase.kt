package com.skytrace.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skytrace.app.data.local.dao.*
import com.skytrace.app.data.local.entity.*

@Database(
    entities = [
        ObservationEntity::class,
        CollectionEntity::class,
        AsteroidCandidateEntity::class,
        SatelliteTleEntity::class,
        CatalogCacheEntity::class,
        StarMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SkyTraceDatabase : RoomDatabase() {
    abstract fun observationDao(): ObservationDao
    abstract fun collectionDao(): CollectionDao
    abstract fun asteroidCandidateDao(): AsteroidCandidateDao
    abstract fun satelliteTleDao(): SatelliteTleDao
    abstract fun catalogCacheDao(): CatalogCacheDao
    abstract fun starMessageDao(): StarMessageDao
}
