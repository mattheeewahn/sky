package com.skytrace.app.services.sky

import com.skytrace.app.domain.model.MoonPhase
import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.domain.model.Phase
import kotlin.math.*

/**
 * Core astronomy calculation engine.
 * Implements real astronomical algorithms for position calculation,
 * coordinate transforms, and celestial mechanics.
 *
 * Based on Jean Meeus "Astronomical Algorithms" (2nd ed.)
 */
object AstronomyEngine {

    private const val DEG_TO_RAD = PI / 180.0
    private const val RAD_TO_DEG = 180.0 / PI
    private const val HOURS_TO_RAD = PI / 12.0
    private const val RAD_TO_HOURS = 12.0 / PI
    private const val J2000 = 2451545.0 // Julian date of J2000.0 epoch
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Convert calendar date/time (UTC) to Julian Date.
     */
    fun dateToJD(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Double = 0.0): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = (y / 100)
        val b = 2 - a + (a / 4)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
        val dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0
        return jd + dayFraction
    }

    /**
     * Convert epoch millis to Julian Date.
     */
    fun epochToJD(epochMillis: Long): Double {
        val days = epochMillis / 86400000.0
        return days + 2440587.5 // Unix epoch in JD
    }

    /**
     * Julian centuries since J2000.0.
     */
    fun julianCenturies(jd: Double): Double = (jd - J2000) / 36525.0

    /**
     * Greenwich Mean Sidereal Time in degrees.
     */
    fun gmst(jd: Double): Double {
        val t = julianCenturies(jd)
        var gmst = 280.46061837 + 360.98564736629 * (jd - J2000) +
                0.000387933 * t * t - t * t * t / 38710000.0
        gmst = gmst.mod(360.0)
        if (gmst < 0) gmst += 360.0
        return gmst
    }

    /**
     * Local Sidereal Time in degrees.
     */
    fun lst(jd: Double, longitudeDeg: Double): Double {
        return (gmst(jd) + longitudeDeg).mod(360.0)
    }

    /**
     * Convert equatorial (RA/Dec) to horizontal (Alt/Az) coordinates.
     * @param raHours Right Ascension in hours
     * @param decDeg Declination in degrees
     * @param latDeg Observer latitude in degrees
     * @param lstDeg Local Sidereal Time in degrees
     * @return Pair(altitude in degrees, azimuth in degrees from North)
     */
    fun equatorialToHorizontal(raHours: Double, decDeg: Double, latDeg: Double, lstDeg: Double): Pair<Double, Double> {
        val ha = (lstDeg - raHours * 15.0).mod(360.0) * DEG_TO_RAD
        val dec = decDeg * DEG_TO_RAD
        val lat = latDeg * DEG_TO_RAD

        val sinAlt = sin(dec) * sin(lat) + cos(dec) * cos(lat) * cos(ha)
        val alt = asin(sinAlt)

        val cosAz = (sin(dec) - sin(alt) * sin(lat)) / (cos(alt) * cos(lat))
        var az = acos(cosAz.coerceIn(-1.0, 1.0))
        if (sin(ha) > 0) az = 2 * PI - az

        return Pair(alt * RAD_TO_DEG, az * RAD_TO_DEG)
    }

    /**
     * Convert horizontal (Alt/Az) to equatorial (RA/Dec).
     */
    fun horizontalToEquatorial(altDeg: Double, azDeg: Double, latDeg: Double, lstDeg: Double): Pair<Double, Double> {
        val alt = altDeg * DEG_TO_RAD
        val az = azDeg * DEG_TO_RAD
        val lat = latDeg * DEG_TO_RAD

        val sinDec = sin(alt) * sin(lat) + cos(alt) * cos(lat) * cos(az)
        val dec = asin(sinDec)

        val cosH = (sin(alt) - sin(dec) * sin(lat)) / (cos(dec) * cos(lat))
        var h = acos(cosH.coerceIn(-1.0, 1.0))
        if (sin(az) > 0) h = 2 * PI - h

        val ra = (lstDeg - h * RAD_TO_DEG).mod(360.0) / 15.0
        return Pair(ra, dec * RAD_TO_DEG)
    }

    /**
     * Calculate angular separation between two points on the celestial sphere.
     * @return separation in degrees
     */
    fun angularSeparation(ra1Hours: Double, dec1Deg: Double, ra2Hours: Double, dec2Deg: Double): Double {
        val ra1 = ra1Hours * HOURS_TO_RAD
        val dec1 = dec1Deg * DEG_TO_RAD
        val ra2 = ra2Hours * HOURS_TO_RAD
        val dec2 = dec2Deg * DEG_TO_RAD

        val cosD = sin(dec1) * sin(dec2) + cos(dec1) * cos(dec2) * cos(ra1 - ra2)
        return acos(cosD.coerceIn(-1.0, 1.0)) * RAD_TO_DEG
    }

    /**
     * Calculate Moon phase for a given Julian Date.
     * Uses simplified Meeus algorithm.
     */
    fun calculateMoonPhase(jd: Double): MoonPhase {
        val t = julianCenturies(jd)

        // Sun's mean longitude
        val sunLong = (280.4664567 + 360007.6982779 * t / 100.0).mod(360.0)
        // Moon's mean longitude
        val moonLong = (218.3164477 + 481267.88123421 * t).mod(360.0)
        // Moon's mean elongation
        val d = (moonLong - sunLong).mod(360.0)

        val dRad = d * DEG_TO_RAD
        val illumination = (1 - cos(dRad)) / 2.0
        val age = d / 12.1907 // approximate days since new moon

        val phase = when {
            age < 1.85 -> Phase.NEW_MOON
            age < 5.53 -> Phase.WAXING_CRESCENT
            age < 9.22 -> Phase.FIRST_QUARTER
            age < 12.91 -> Phase.WAXING_GIBBOUS
            age < 16.61 -> Phase.FULL_MOON
            age < 20.30 -> Phase.WANING_GIBBOUS
            age < 23.99 -> Phase.LAST_QUARTER
            age < 27.68 -> Phase.WANING_CRESCENT
            else -> Phase.NEW_MOON
        }

        val emoji = when (phase) {
            Phase.NEW_MOON -> "🌑"
            Phase.WAXING_CRESCENT -> "🌒"
            Phase.FIRST_QUARTER -> "🌓"
            Phase.WAXING_GIBBOUS -> "🌔"
            Phase.FULL_MOON -> "🌕"
            Phase.WANING_GIBBOUS -> "🌖"
            Phase.LAST_QUARTER -> "🌗"
            Phase.WANING_CRESCENT -> "🌘"
        }

        return MoonPhase(
            phase = phase,
            illumination = illumination,
            age = age,
            name = phase.label,
            emoji = emoji
        )
    }

    /**
     * Calculate Sun position (RA, Dec) for a given JD.
     * Simplified algorithm accurate to ~1 arcmin.
     */
    fun sunPosition(jd: Double): Pair<Double, Double> {
        val t = julianCenturies(jd)
        val l0 = (280.46646 + 36000.76983 * t + 0.0003032 * t * t).mod(360.0)
        val m = (357.52911 + 35999.05029 * t - 0.0001537 * t * t).mod(360.0)
        val mRad = m * DEG_TO_RAD

        val c = (1.914602 - 0.004817 * t) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2 * mRad) +
                0.000289 * sin(3 * mRad)

        val sunLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val apparentLong = (sunLong - 0.00569 - 0.00478 * sin(omega * DEG_TO_RAD))

        val obliquity = 23.439291 - 0.0130042 * t
        val oblRad = obliquity * DEG_TO_RAD
        val appLongRad = apparentLong * DEG_TO_RAD

        val ra = atan2(cos(oblRad) * sin(appLongRad), cos(appLongRad)) * RAD_TO_HOURS
        val dec = asin(sin(oblRad) * sin(appLongRad)) * RAD_TO_DEG

        return Pair(if (ra < 0) ra + 24.0 else ra, dec)
    }

    /**
     * Calculate Moon position (RA, Dec) for a given JD.
     * Simplified algorithm.
     */
    fun moonPosition(jd: Double): Pair<Double, Double> {
        val t = julianCenturies(jd)

        val l = (218.3164477 + 481267.88123421 * t).mod(360.0)
        val m = (134.9633964 + 477198.8675055 * t).mod(360.0)
        val mp = (357.5291092 + 35999.0502909 * t).mod(360.0)
        val d = (297.8501921 + 445267.1114034 * t).mod(360.0)
        val f = (93.272095 + 483202.0175233 * t).mod(360.0)

        val mRad = m * DEG_TO_RAD
        val mpRad = mp * DEG_TO_RAD
        val dRad = d * DEG_TO_RAD
        val fRad = f * DEG_TO_RAD

        val longitude = l +
                6.289 * sin(mRad) +
                1.274 * sin(2 * dRad - mRad) +
                0.658 * sin(2 * dRad) +
                0.214 * sin(2 * mRad) -
                0.186 * sin(mpRad) -
                0.114 * sin(2 * fRad)

        val latitude = 5.128 * sin(fRad) +
                0.281 * sin(mRad + fRad) +
                0.278 * sin(mRad - fRad)

        val obliquity = 23.439291 - 0.0130042 * t
        val eclLong = longitude * DEG_TO_RAD
        val eclLat = latitude * DEG_TO_RAD
        val oblRad = obliquity * DEG_TO_RAD

        val ra = atan2(
            sin(eclLong) * cos(oblRad) - tan(eclLat) * sin(oblRad),
            cos(eclLong)
        ) * RAD_TO_HOURS
        val dec = asin(
            sin(eclLat) * cos(oblRad) + cos(eclLat) * sin(oblRad) * sin(eclLong)
        ) * RAD_TO_DEG

        return Pair(if (ra < 0) ra + 24.0 else ra, dec)
    }

    /**
     * Calculate planet positions using simplified VSOP87 elements.
     * Returns heliocentric ecliptic longitude/latitude then converts to geocentric RA/Dec.
     */
    fun planetPosition(planet: PlanetElements, jd: Double): Pair<Double, Double> {
        val t = julianCenturies(jd)

        // Mean elements
        val l = (planet.l0 + planet.l1 * t).mod(360.0)
        val a = planet.a0 + planet.a1 * t
        val e = planet.e0 + planet.e1 * t
        val i = planet.i0 + planet.i1 * t
        val omega = (planet.omega0 + planet.omega1 * t).mod(360.0)
        val longPeri = (planet.pi0 + planet.pi1 * t).mod(360.0)

        // Mean anomaly
        val m = (l - longPeri).mod(360.0)
        val mRad = m * DEG_TO_RAD

        // Solve Kepler's equation iteratively
        var eAnomaly = mRad + e * sin(mRad) * (1.0 + e * cos(mRad))
        for (iter in 0..5) {
            val dE = (eAnomaly - e * sin(eAnomaly) - mRad) / (1 - e * cos(eAnomaly))
            eAnomaly -= dE
            if (abs(dE) < 1e-9) break
        }

        // True anomaly
        val xv = a * (cos(eAnomaly) - e)
        val yv = a * sqrt(1.0 - e * e) * sin(eAnomaly)
        val v = atan2(yv, xv)
        val r = sqrt(xv * xv + yv * yv)

        // Heliocentric ecliptic coordinates
        val omegaRad = omega * DEG_TO_RAD
        val iRad = i * DEG_TO_RAD
        val longPeriRad = longPeri * DEG_TO_RAD
        val w = longPeriRad - omegaRad

        val xh = r * (cos(omegaRad) * cos(v + w) - sin(omegaRad) * sin(v + w) * cos(iRad))
        val yh = r * (sin(omegaRad) * cos(v + w) + cos(omegaRad) * sin(v + w) * cos(iRad))
        val zh = r * sin(v + w) * sin(iRad)

        // Get Earth position
        val earthElements = PlanetElements.EARTH
        val lE = (earthElements.l0 + earthElements.l1 * t).mod(360.0)
        val aE = earthElements.a0
        val eE = earthElements.e0 + earthElements.e1 * t
        val piE = (earthElements.pi0 + earthElements.pi1 * t).mod(360.0)
        val mE = (lE - piE).mod(360.0) * DEG_TO_RAD

        var eAnomalyE = mE + eE * sin(mE)
        for (iter in 0..5) {
            val dE = (eAnomalyE - eE * sin(eAnomalyE) - mE) / (1 - eE * cos(eAnomalyE))
            eAnomalyE -= dE
        }

        val xvE = aE * (cos(eAnomalyE) - eE)
        val yvE = aE * sqrt(1.0 - eE * eE) * sin(eAnomalyE)
        val vE = atan2(yvE, xvE)
        val rE = sqrt(xvE * xvE + yvE * yvE)

        val xs = rE * cos(vE + piE * DEG_TO_RAD)
        val ys = rE * sin(vE + piE * DEG_TO_RAD)

        // Geocentric ecliptic
        val xg = xh - xs
        val yg = yh - ys
        val zg = zh

        // Ecliptic to equatorial
        val obliquity = 23.439291 * DEG_TO_RAD
        val xeq = xg
        val yeq = yg * cos(obliquity) - zg * sin(obliquity)
        val zeq = yg * sin(obliquity) + zg * cos(obliquity)

        val ra = atan2(yeq, xeq) * RAD_TO_HOURS
        val dec = atan2(zeq, sqrt(xeq * xeq + yeq * yeq)) * RAD_TO_DEG

        return Pair(if (ra < 0) ra + 24.0 else ra, dec)
    }

    /**
     * Calculate rise/set times for an object.
     * @return Pair(rise epoch millis, set epoch millis) or null if circumpolar/never rises
     */
    fun calculateRiseSet(
        raHours: Double,
        decDeg: Double,
        location: ObserverLocation,
        jd: Double,
        altitudeThreshold: Double = -0.5667 // standard refraction
    ): Pair<Long?, Long?> {
        val lat = location.latitude * DEG_TO_RAD
        val dec = decDeg * DEG_TO_RAD
        val threshold = altitudeThreshold * DEG_TO_RAD

        val cosH = (sin(threshold) - sin(lat) * sin(dec)) / (cos(lat) * cos(dec))

        if (cosH > 1.0) return Pair(null, null) // never rises
        if (cosH < -1.0) return Pair(null, null) // circumpolar

        val h = acos(cosH) * RAD_TO_DEG / 15.0 // in hours

        val lstNoon = lst(jd, location.longitude) / 15.0
        val transit = raHours - lstNoon / 1.0

        val riseHours = (transit - h).mod(24.0)
        val setHours = (transit + h).mod(24.0)

        // Convert to epoch (approximate)
        val baseEpoch = ((jd - 2440587.5) * 86400000).toLong()
        val riseEpoch = baseEpoch + (riseHours * 3600000).toLong()
        val setEpoch = baseEpoch + (setHours * 3600000).toLong()

        return Pair(riseEpoch, setEpoch)
    }

    /**
     * Normalize angle to 0-360 range.
     */
    fun normalizeAngle(deg: Double): Double = deg.mod(360.0).let { if (it < 0) it + 360.0 else it }

    /**
     * Format RA as HH:MM:SS string.
     */
    fun formatRA(hours: Double): String {
        val h = hours.toInt()
        val m = ((hours - h) * 60).toInt()
        val s = ((hours - h - m / 60.0) * 3600).toInt()
        return "%02dh %02dm %02ds".format(h, m, s)
    }

    /**
     * Format Dec as ±DD°MM'SS" string.
     */
    fun formatDec(degrees: Double): String {
        val sign = if (degrees >= 0) "+" else "-"
        val absDeg = abs(degrees)
        val d = absDeg.toInt()
        val m = ((absDeg - d) * 60).toInt()
        val s = ((absDeg - d - m / 60.0) * 3600).toInt()
        return "$sign%02d°%02d'%02d\"".format(d, m, s)
    }
}
