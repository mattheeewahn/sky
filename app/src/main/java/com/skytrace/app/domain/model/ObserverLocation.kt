package com.skytrace.app.domain.model

/**
 * The observer's geographic location and local conditions.
 */
data class ObserverLocation(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
