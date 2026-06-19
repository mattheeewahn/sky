package com.skytrace.app.domain.model

/**
 * A recorded observation by the user.
 */
data class Observation(
    val id: Long = 0,
    val objectName: String,
    val catalogId: String? = null,
    val objectType: ObjectType,
    val dateTime: Long, // epoch millis
    val latitude: Double,
    val longitude: Double,
    val telescope: String? = null,
    val eyepiece: String? = null,
    val camera: String? = null,
    val filter: String? = null,
    val exposureSeconds: Double? = null,
    val seeingCondition: SeeingCondition? = null,
    val transparency: Transparency? = null,
    val skyBrightness: SkyBrightness? = null,
    val notes: String? = null,
    val photoUris: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SeeingCondition(val label: String, val arcSeconds: String) {
    EXCELLENT("Excellent", "< 1\""),
    GOOD("Good", "1-2\""),
    FAIR("Fair", "2-3\""),
    POOR("Poor", "3-5\""),
    TERRIBLE("Terrible", "> 5\"")
}

enum class Transparency(val label: String) {
    EXCELLENT("Excellent - Mag 6.5+"),
    GOOD("Good - Mag 5.5-6.5"),
    FAIR("Fair - Mag 4.5-5.5"),
    POOR("Poor - Mag 3.5-4.5"),
    VERY_POOR("Very Poor - Mag < 3.5")
}

enum class SkyBrightness(val label: String, val bortleClass: Int) {
    EXCELLENT_DARK("Excellent Dark Site", 1),
    TYPICAL_DARK("Typical Dark Site", 2),
    RURAL("Rural Sky", 3),
    RURAL_SUBURBAN("Rural/Suburban Transition", 4),
    SUBURBAN("Suburban Sky", 5),
    BRIGHT_SUBURBAN("Bright Suburban", 6),
    SUBURBAN_URBAN("Suburban/Urban Transition", 7),
    CITY("City Sky", 8),
    INNER_CITY("Inner City Sky", 9)
}
