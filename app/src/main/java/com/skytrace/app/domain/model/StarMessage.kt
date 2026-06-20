package com.skytrace.app.domain.model

/**
 * A message "transmitted" toward a celestial object.
 * This represents the user's purchase of a symbolic/real RF transmission.
 */
data class StarMessage(
    val id: Long = 0,
    val targetName: String,
    val targetCatalogId: String? = null,
    val targetType: ObjectType,
    val targetRA: Double, // hours
    val targetDec: Double, // degrees
    val distanceLightYears: Double? = null,
    val message: String,
    val senderName: String,
    val recipientName: String? = null, // "To: ___" on the certificate
    val transmissionTime: Long = System.currentTimeMillis(),
    val frequency: Double = 1420.405, // MHz (Hydrogen line - universal)
    val estimatedArrivalYears: Double? = null,
    val status: TransmissionStatus = TransmissionStatus.QUEUED,
    val certificateId: String? = null, // unique ID for certificate
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransmissionStatus(val label: String, val emoji: String) {
    QUEUED("Queued", "⏳"),
    TRANSMITTING("Transmitting", "📡"),
    TRANSMITTED("Transmitted", "✅"),
    TRAVELING("Traveling through space", "🚀")
}

/**
 * Pricing tiers for the transmission service.
 */
enum class MessageTier(
    val label: String,
    val maxChars: Int,
    val priceKRW: Int,
    val description: String
) {
    WHISPER("Whisper", 50, 1000, "A short thought to the stars"),
    MESSAGE("Message", 200, 3000, "Your words, echoing through space"),
    LETTER("Letter", 1000, 5000, "A letter that will travel for eternity"),
    LEGACY("Legacy", 5000, 10000, "Your legacy, encoded in radio waves forever")
}
