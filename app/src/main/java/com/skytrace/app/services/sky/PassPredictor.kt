package com.skytrace.app.services.sky

import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.domain.model.SatellitePass
import com.skytrace.app.domain.model.TLEData

/**
 * Satellite pass predictor using the full SGP4 model.
 * Predicts visible passes above a given elevation threshold.
 */
object PassPredictor {

    /**
     * Predict all passes for a satellite over a time range.
     * Uses SGP4 propagation for accurate position calculation.
     *
     * @param tle The satellite TLE data
     * @param observer The observer's location
     * @param startEpochMillis Start of prediction window (millis)
     * @param durationHours Duration to search (hours)
     * @param minElevation Minimum culmination elevation to report (degrees)
     * @param stepSeconds Time step for scanning (seconds)
     */
    fun predictPasses(
        tle: TLEData,
        observer: ObserverLocation,
        startEpochMillis: Long,
        durationHours: Int = 24,
        minElevation: Double = 10.0,
        stepSeconds: Int = 30
    ): List<SatellitePass> {
        val sgp4 = try { SGP4(tle) } catch (e: Exception) { return emptyList() }

        val passes = mutableListOf<SatellitePass>()
        val tleEpochMillis = tleToEpochMillis(tle)
        val stepMinutes = stepSeconds / 60.0
        val totalMinutes = durationHours * 60.0

        var wasAbove = false
        var riseTime = 0L
        var riseAz = 0.0
        var maxElev = 0.0
        var maxElevTime = 0L
        var maxElevAz = 0.0

        var t = 0.0
        while (t < totalMinutes) {
            val currentMillis = startEpochMillis + (t * 60000).toLong()
            val tsince = (currentMillis - tleEpochMillis) / 60000.0 // minutes since TLE epoch

            try {
                val result = sgp4.propagate(tsince)
                val pos = sgp4.getObserverView(result, observer, tsince)

                if (pos.elevation > 0 && !wasAbove) {
                    // Satellite just rose
                    wasAbove = true
                    riseTime = currentMillis
                    riseAz = pos.azimuth
                    maxElev = pos.elevation
                    maxElevTime = currentMillis
                    maxElevAz = pos.azimuth
                } else if (pos.elevation > 0 && wasAbove) {
                    if (pos.elevation > maxElev) {
                        maxElev = pos.elevation
                        maxElevTime = currentMillis
                        maxElevAz = pos.azimuth
                    }
                } else if (pos.elevation <= 0 && wasAbove) {
                    // Satellite just set
                    wasAbove = false
                    if (maxElev >= minElevation) {
                        passes.add(
                            SatellitePass(
                                satellite = tle,
                                riseTime = riseTime,
                                riseAzimuth = riseAz,
                                culminationTime = maxElevTime,
                                culminationAltitude = maxElev,
                                culminationAzimuth = maxElevAz,
                                setTime = currentMillis,
                                setAzimuth = pos.azimuth,
                                isVisible = isSunlit(currentMillis, observer, pos.altitude)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Propagation error for this timestep, skip
            }

            t += stepMinutes
        }

        return passes
    }

    /**
     * Check if satellite is sunlit (simplified).
     * A satellite is visible if it's above observer's horizon,
     * the observer is in darkness, but the satellite is still in sunlight.
     */
    private fun isSunlit(timeMillis: Long, observer: ObserverLocation, satAltKm: Double): Boolean {
        val jd = AstronomyEngine.epochToJD(timeMillis)
        val (sunRa, sunDec) = AstronomyEngine.sunPosition(jd)
        val lstDeg = AstronomyEngine.lst(jd, observer.longitude)
        val (sunAlt, _) = AstronomyEngine.equatorialToHorizontal(sunRa, sunDec, observer.latitude, lstDeg)

        // Observer must be in twilight or darkness (sun below -6°)
        if (sunAlt > -6.0) return false

        // Satellite at >200km altitude is usually sunlit when observer is in twilight
        // More precise: check Earth's shadow cone at satellite altitude
        // Simplified: satellites above ~200km are sunlit until sun is ~18° below horizon
        val shadowAngle = Math.toDegrees(
            Math.acos(6371.0 / (6371.0 + satAltKm))
        )
        return sunAlt > -(90.0 - shadowAngle + 18.0)
    }

    private fun tleToEpochMillis(tle: TLEData): Long {
        val epochStr = tle.line1.substring(18, 32).trim()
        val yr = epochStr.substring(0, 2).toIntOrNull() ?: 0
        val year = if (yr < 57) 2000 + yr else 1900 + yr
        val days = epochStr.substring(2).toDoubleOrNull() ?: 0.0

        val cal = java.util.GregorianCalendar(year, 0, 1)
        return cal.timeInMillis + ((days - 1) * 86400000).toLong()
    }

    /**
     * Get satellite's current RA/Dec for sky map display.
     */
    fun getSatelliteRaDec(
        tle: TLEData,
        observer: ObserverLocation,
        timeMillis: Long = System.currentTimeMillis()
    ): Pair<Double, Double>? {
        return try {
            val sgp4 = SGP4(tle)
            val tleEpochMillis = tleToEpochMillis(tle)
            val tsince = (timeMillis - tleEpochMillis) / 60000.0

            val result = sgp4.propagate(tsince)
            val pos = sgp4.getObserverView(result, observer, tsince)

            if (pos.elevation > 0) {
                // Convert alt/az back to RA/Dec for display
                val jd = AstronomyEngine.epochToJD(timeMillis)
                val lstDeg = AstronomyEngine.lst(jd, observer.longitude)
                val (ra, dec) = AstronomyEngine.horizontalToEquatorial(
                    pos.elevation, pos.azimuth, observer.latitude, lstDeg
                )
                Pair(ra, dec)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
