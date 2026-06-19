package com.skytrace.app.domain.model

/**
 * Unified representation of any celestial object visible in the sky.
 * Used across sky map, search, object detail, and telescope pointing.
 */
data class CelestialObject(
    val id: String,
    val name: String,
    val catalogId: String? = null,
    val type: ObjectType,
    val rightAscension: Double, // hours (0-24)
    val declination: Double, // degrees (-90 to +90)
    val magnitude: Double? = null,
    val altitude: Double? = null, // degrees above horizon
    val azimuth: Double? = null, // degrees from north
    val riseTime: Long? = null, // epoch millis
    val setTime: Long? = null, // epoch millis
    val isVisible: Boolean = false,
    val distanceAU: Double? = null,
    val constellation: String? = null,
    val description: String? = null,
    val extraData: Map<String, String> = emptyMap()
)

enum class ObjectType {
    STAR,
    PLANET,
    MOON,
    SUN,
    MESSIER,
    NGC,
    ASTEROID,
    COMET,
    SATELLITE,
    DEEP_SKY,
    UNKNOWN
}
