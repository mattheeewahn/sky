package com.skytrace.app.services.sky

import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.domain.model.SatellitePass
import com.skytrace.app.domain.model.TLEData
import kotlin.math.*

/**
 * Simplified SGP4-like satellite propagator for TLE data.
 * Calculates satellite position and pass predictions.
 *
 * This is a simplified implementation suitable for pass prediction
 * with accuracy of a few seconds in timing.
 */
object SatellitePropagator {

    private const val DEG_TO_RAD = Math.PI / 180.0
    private const val RAD_TO_DEG = 180.0 / Math.PI
    private const val MINUTES_PER_DAY = 1440.0
    private const val TWO_PI = 2.0 * Math.PI
    private const val EARTH_RADIUS_KM = 6378.137
    private const val MU = 398600.4418 // km^3/s^2
    private const val J2 = 0.00108263

    /**
     * Calculate satellite ECI position at given time.
     * Returns (x, y, z) in km from Earth center.
     */
    fun propagate(tle: TLEData, minutesSinceEpoch: Double): Triple<Double, Double, Double> {
        val n0 = tle.meanMotion * TWO_PI / MINUTES_PER_DAY // rad/min
        val a0 = (MU / (n0 * n0 / 3600.0)).pow(1.0 / 3.0) // semi-major axis approximation

        val incl = tle.inclination * DEG_TO_RAD
        val raan0 = tle.raan * DEG_TO_RAD
        val argp0 = tle.argPerigee * DEG_TO_RAD
        val ecc = tle.eccentricity
        val m0 = tle.meanAnomaly * DEG_TO_RAD

        // J2 perturbations
        val p = a0 * (1 - ecc * ecc)
        val raanDot = -1.5 * J2 * (EARTH_RADIUS_KM / p).pow(2) * n0 * cos(incl)
        val argpDot = 0.75 * J2 * (EARTH_RADIUS_KM / p).pow(2) * n0 * (5 * cos(incl).pow(2) - 1)

        // Propagate
        val dt = minutesSinceEpoch
        val m = (m0 + n0 * dt) % TWO_PI
        val raan = raan0 + raanDot * dt
        val argp = argp0 + argpDot * dt

        // Solve Kepler's equation
        var eAnomaly = m
        for (i in 0..10) {
            val dE = (m - (eAnomaly - ecc * sin(eAnomaly))) / (1 - ecc * cos(eAnomaly))
            eAnomaly += dE
            if (abs(dE) < 1e-10) break
        }

        // True anomaly
        val sinV = sqrt(1 - ecc * ecc) * sin(eAnomaly) / (1 - ecc * cos(eAnomaly))
        val cosV = (cos(eAnomaly) - ecc) / (1 - ecc * cos(eAnomaly))
        val trueAnomaly = atan2(sinV, cosV)

        // Distance
        val r = a0 * (1 - ecc * cos(eAnomaly))

        // Position in orbital plane
        val xOrb = r * cos(trueAnomaly)
        val yOrb = r * sin(trueAnomaly)

        // Rotate to ECI
        val cosRaan = cos(raan)
        val sinRaan = sin(raan)
        val cosArgp = cos(argp)
        val sinArgp = sin(argp)
        val cosIncl = cos(incl)
        val sinIncl = sin(incl)

        val x = xOrb * (cosRaan * cosArgp - sinRaan * sinArgp * cosIncl) -
                yOrb * (cosRaan * sinArgp + sinRaan * cosArgp * cosIncl)
        val y = xOrb * (sinRaan * cosArgp + cosRaan * sinArgp * cosIncl) -
                yOrb * (sinRaan * sinArgp - cosRaan * cosArgp * cosIncl)
        val z = xOrb * sinArgp * sinIncl + yOrb * cosArgp * sinIncl

        return Triple(x, y, z)
    }

    /**
     * Convert ECI position to observer-relative Alt/Az.
     */
    fun eciToAltAz(
        eciX: Double, eciY: Double, eciZ: Double,
        location: ObserverLocation,
        gmstDeg: Double
    ): Pair<Double, Double> {
        val lat = location.latitude * DEG_TO_RAD
        val lon = location.longitude * DEG_TO_RAD
        val gmst = gmstDeg * DEG_TO_RAD

        // Observer ECI position
        val obsX = EARTH_RADIUS_KM * cos(lat) * cos(gmst + lon)
        val obsY = EARTH_RADIUS_KM * cos(lat) * sin(gmst + lon)
        val obsZ = EARTH_RADIUS_KM * sin(lat)

        // Range vector
        val rx = eciX - obsX
        val ry = eciY - obsY
        val rz = eciZ - obsZ

        // Rotate to topocentric
        val theta = gmst + lon
        val sinLat = sin(lat)
        val cosLat = cos(lat)
        val sinTheta = sin(theta)
        val cosTheta = cos(theta)

        val south = sinLat * cosTheta * rx + sinLat * sinTheta * ry - cosLat * rz
        val east = -sinTheta * rx + cosTheta * ry
        val up = cosLat * cosTheta * rx + cosLat * sinTheta * ry + sinLat * rz

        val range = sqrt(south * south + east * east + up * up)
        val alt = asin(up / range) * RAD_TO_DEG
        var az = atan2(east, -south) * RAD_TO_DEG
        if (az < 0) az += 360.0

        return Pair(alt, az)
    }

    /**
     * Predict visible passes for a satellite over a time range.
     */
    fun predictPasses(
        tle: TLEData,
        location: ObserverLocation,
        startEpochMillis: Long,
        durationHours: Int = 24,
        minAltitude: Double = 10.0
    ): List<SatellitePass> {
        val passes = mutableListOf<SatellitePass>()
        val stepMinutes = 1.0
        val totalMinutes = durationHours * 60.0

        // TLE epoch in minutes since Unix epoch
        val tleYear = if (tle.epoch > 57000) 1900 + (tle.epoch / 1000).toInt()
        else 2000 + (tle.epoch / 1000).toInt()
        val tleDay = tle.epoch % 1000
        val tleEpochMillis = approximateEpochMillis(tleYear, tleDay)

        val startMinutesSinceEpoch = (startEpochMillis - tleEpochMillis) / 60000.0

        var wasAboveHorizon = false
        var passStartMinute = 0.0
        var maxAlt = 0.0
        var maxAltAz = 0.0
        var maxAltMinute = 0.0
        var riseAz = 0.0

        var t = 0.0
        while (t < totalMinutes) {
            val minutesSinceEpoch = startMinutesSinceEpoch + t
            val jd = AstronomyEngine.epochToJD(startEpochMillis + (t * 60000).toLong())
            val gmst = AstronomyEngine.gmst(jd)

            val (x, y, z) = propagate(tle, minutesSinceEpoch)
            val (alt, az) = eciToAltAz(x, y, z, location, gmst)

            if (alt > minAltitude && !wasAboveHorizon) {
                wasAboveHorizon = true
                passStartMinute = t
                riseAz = az
                maxAlt = alt
                maxAltAz = az
                maxAltMinute = t
            } else if (alt > minAltitude && wasAboveHorizon) {
                if (alt > maxAlt) {
                    maxAlt = alt
                    maxAltAz = az
                    maxAltMinute = t
                }
            } else if (alt <= minAltitude && wasAboveHorizon) {
                wasAboveHorizon = false
                val pass = SatellitePass(
                    satellite = tle,
                    riseTime = startEpochMillis + (passStartMinute * 60000).toLong(),
                    riseAzimuth = riseAz,
                    culminationTime = startEpochMillis + (maxAltMinute * 60000).toLong(),
                    culminationAltitude = maxAlt,
                    culminationAzimuth = maxAltAz,
                    setTime = startEpochMillis + (t * 60000).toLong(),
                    setAzimuth = az,
                    isVisible = true
                )
                passes.add(pass)
            }
            t += stepMinutes
        }

        return passes
    }

    private fun approximateEpochMillis(year: Int, dayOfYear: Double): Long {
        val jan1 = java.util.GregorianCalendar(year, 0, 1).timeInMillis
        return jan1 + ((dayOfYear - 1) * 86400000).toLong()
    }
}
