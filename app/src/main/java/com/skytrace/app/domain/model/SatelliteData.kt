package com.skytrace.app.domain.model

/**
 * Two-Line Element data for satellite tracking.
 */
data class TLEData(
    val name: String,
    val noradId: Int,
    val line1: String,
    val line2: String,
    val category: SatelliteCategory,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    // Parse orbital elements from TLE
    val inclination: Double get() = line2.substring(8, 16).trim().toDoubleOrNull() ?: 0.0
    val eccentricity: Double get() {
        val eStr = line2.substring(26, 33).trim()
        return "0.$eStr".toDoubleOrNull() ?: 0.0
    }
    val meanMotion: Double get() = line2.substring(52, 63).trim().toDoubleOrNull() ?: 0.0
    val epoch: Double get() = line1.substring(18, 32).trim().toDoubleOrNull() ?: 0.0
    val raan: Double get() = line2.substring(17, 25).trim().toDoubleOrNull() ?: 0.0
    val argPerigee: Double get() = line2.substring(34, 42).trim().toDoubleOrNull() ?: 0.0
    val meanAnomaly: Double get() = line2.substring(43, 51).trim().toDoubleOrNull() ?: 0.0
}

enum class SatelliteCategory(val label: String, val celestrakGroup: String) {
    ACTIVE("Active Satellites", "active"),
    STARLINK("Starlink", "starlink"),
    ISS("ISS", "stations"),
    GPS("GPS", "gps-ops"),
    WEATHER("Weather", "weather"),
    SCIENCE("Science", "science"),
    DEBRIS("Debris", "cosmos-2251-debris"),
    OTHER("Other", "supplemental")
}

/**
 * Calculated satellite pass prediction.
 */
data class SatellitePass(
    val satellite: TLEData,
    val riseTime: Long,
    val riseAzimuth: Double,
    val culminationTime: Long,
    val culminationAltitude: Double,
    val culminationAzimuth: Double,
    val setTime: Long,
    val setAzimuth: Double,
    val maxMagnitude: Double? = null,
    val isVisible: Boolean = true
)
