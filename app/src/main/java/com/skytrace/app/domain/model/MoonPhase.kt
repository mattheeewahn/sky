package com.skytrace.app.domain.model

/**
 * Moon phase information.
 */
data class MoonPhase(
    val phase: Phase,
    val illumination: Double, // 0.0 to 1.0
    val age: Double, // days since new moon
    val name: String,
    val emoji: String
)

enum class Phase(val label: String) {
    NEW_MOON("New Moon"),
    WAXING_CRESCENT("Waxing Crescent"),
    FIRST_QUARTER("First Quarter"),
    WAXING_GIBBOUS("Waxing Gibbous"),
    FULL_MOON("Full Moon"),
    WANING_GIBBOUS("Waning Gibbous"),
    LAST_QUARTER("Last Quarter"),
    WANING_CRESCENT("Waning Crescent")
}
