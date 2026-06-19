package com.skytrace.app.domain.model

/**
 * An asteroid candidate report created from user observations.
 */
data class AsteroidCandidate(
    val id: Long = 0,
    val observationTime: Long,
    val latitude: Double,
    val longitude: Double,
    val telescope: String? = null,
    val camera: String? = null,
    val exposureSeconds: Double? = null,
    val fieldOfViewArcmin: Double? = null,
    val centerRA: Double? = null, // hours
    val centerDec: Double? = null, // degrees
    val plateScaleArcsecPerPixel: Double? = null,
    val notes: String? = null,
    val imageUris: List<String> = emptyList(),
    val status: CandidateStatus = CandidateStatus.DRAFT,
    val verificationResult: VerificationResult? = null,
    val markedPositionX: Float? = null,
    val markedPositionY: Float? = null,
    val estimatedMovementDegPerHour: Double? = null,
    val movementDirectionDeg: Double? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CandidateStatus(val label: String) {
    DRAFT("Draft"),
    CHECKING("Checking"),
    LIKELY_KNOWN_ASTEROID("Likely Known Asteroid"),
    LIKELY_SATELLITE("Likely Satellite"),
    LIKELY_COMET("Likely Comet"),
    LIKELY_STAR_PLANET("Likely Star/Planet"),
    LIKELY_ARTIFACT("Likely Image Artifact"),
    UNKNOWN_CANDIDATE("Unknown Candidate"),
    NEEDS_FOLLOWUP("Needs Follow-up")
}

/**
 * Result from automatic verification against known databases.
 */
data class VerificationResult(
    val bestAsteroidMatch: KnownObjectMatch? = null,
    val bestSatelliteMatch: KnownObjectMatch? = null,
    val possibleCometMatch: KnownObjectMatch? = null,
    val possiblePlanetStarMatch: KnownObjectMatch? = null,
    val noKnownMatch: Boolean = false,
    val checkedAt: Long = System.currentTimeMillis()
)

data class KnownObjectMatch(
    val name: String,
    val catalogId: String? = null,
    val angularSeparationArcsec: Double,
    val predictedRA: Double,
    val predictedDec: Double,
    val observedRA: Double,
    val observedDec: Double,
    val confidencePercent: Int, // 0-100
    val explanation: String
)
