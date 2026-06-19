package com.skytrace.app.domain.model

/**
 * Tracks which objects the user has observed.
 */
data class CollectionEntry(
    val id: Long = 0,
    val objectName: String,
    val catalogId: String? = null,
    val objectType: ObjectType,
    val firstObserved: Long,
    val lastObserved: Long,
    val observationCount: Int = 1,
    val notes: String? = null
)

data class CollectionSummary(
    val totalPlanets: Int = 8,
    val observedPlanets: Int = 0,
    val totalMessier: Int = 110,
    val observedMessier: Int = 0,
    val totalNGC: Int = 7840,
    val observedNGC: Int = 0,
    val totalAsteroids: Int = 0,
    val observedAsteroids: Int = 0,
    val totalComets: Int = 0,
    val observedComets: Int = 0,
    val totalSatellites: Int = 0,
    val observedSatellites: Int = 0,
    val moonObserved: Boolean = false
) {
    val overallObserved: Int get() = observedPlanets + observedMessier + observedNGC +
            observedAsteroids + observedComets + observedSatellites + if (moonObserved) 1 else 0
}
