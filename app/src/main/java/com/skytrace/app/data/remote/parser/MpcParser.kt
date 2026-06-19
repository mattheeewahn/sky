package com.skytrace.app.data.remote.parser

import com.google.gson.JsonParser
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType

/**
 * Parser for Minor Planet Center API responses.
 * Handles MPCORB JSON format and HTML table scraping for NEO data.
 */
object MpcParser {

    data class MpcOrbit(
        val designation: String,
        val name: String?,
        val number: Int?,
        val epoch: Double,
        val meanAnomaly: Double,
        val argPerihelion: Double,
        val longAscNode: Double,
        val inclination: Double,
        val eccentricity: Double,
        val semiMajorAxis: Double,
        val absoluteMag: Double?,
        val orbitType: String?
    )

    /**
     * Parse MPC orbit search JSON response.
     * Format: array of objects with orbital elements.
     */
    fun parseOrbitSearch(json: String): List<MpcOrbit> {
        val results = mutableListOf<MpcOrbit>()
        try {
            val root = JsonParser.parseString(json)
            val array = when {
                root.isJsonArray -> root.asJsonArray
                root.isJsonObject && root.asJsonObject.has("results") ->
                    root.asJsonObject.getAsJsonArray("results")
                root.isJsonObject && root.asJsonObject.has("data") ->
                    root.asJsonObject.getAsJsonArray("data")
                else -> return emptyList()
            }

            for (element in array) {
                val obj = element.asJsonObject
                results.add(
                    MpcOrbit(
                        designation = obj.get("designation")?.asString
                            ?: obj.get("des")?.asString ?: "",
                        name = obj.get("name")?.asString,
                        number = obj.get("number")?.asInt,
                        epoch = obj.get("epoch")?.asDouble ?: 0.0,
                        meanAnomaly = obj.get("mean_anomaly")?.asDouble
                            ?: obj.get("M")?.asDouble ?: 0.0,
                        argPerihelion = obj.get("arg_perihelion")?.asDouble
                            ?: obj.get("peri")?.asDouble ?: 0.0,
                        longAscNode = obj.get("long_asc_node")?.asDouble
                            ?: obj.get("node")?.asDouble ?: 0.0,
                        inclination = obj.get("inclination")?.asDouble
                            ?: obj.get("i")?.asDouble ?: 0.0,
                        eccentricity = obj.get("eccentricity")?.asDouble
                            ?: obj.get("e")?.asDouble ?: 0.0,
                        semiMajorAxis = obj.get("semi_major_axis")?.asDouble
                            ?: obj.get("a")?.asDouble ?: 0.0,
                        absoluteMag = obj.get("absolute_magnitude")?.asDouble
                            ?: obj.get("H")?.asDouble,
                        orbitType = obj.get("orbit_type")?.asString
                    )
                )
            }
        } catch (e: Exception) {
            // Parse error - return empty
        }
        return results
    }

    /**
     * Parse MPCORB fixed-width format (the standard .DAT file).
     * Each line is 202 characters with fixed-column orbital data.
     */
    fun parseMpcorbLine(line: String): MpcOrbit? {
        if (line.length < 160) return null
        if (line.startsWith("-") || line.startsWith("Des")) return null

        return try {
            val designation = line.substring(0, 7).trim()
            val h = line.substring(8, 13).trim().toDoubleOrNull()
            val epoch = line.substring(20, 25).trim().toDoubleOrNull() ?: 0.0
            val m = line.substring(26, 35).trim().toDoubleOrNull() ?: 0.0
            val peri = line.substring(37, 46).trim().toDoubleOrNull() ?: 0.0
            val node = line.substring(48, 57).trim().toDoubleOrNull() ?: 0.0
            val incl = line.substring(59, 68).trim().toDoubleOrNull() ?: 0.0
            val ecc = line.substring(70, 79).trim().toDoubleOrNull() ?: 0.0
            val a = line.substring(92, 103).trim().toDoubleOrNull() ?: 0.0

            val number = line.substring(166, 173).trim().toIntOrNull()
            val name = if (line.length > 175) line.substring(175).trim().ifBlank { null } else null

            MpcOrbit(
                designation = designation,
                name = name,
                number = number,
                epoch = epoch,
                meanAnomaly = m,
                argPerihelion = peri,
                longAscNode = node,
                inclination = incl,
                eccentricity = ecc,
                semiMajorAxis = a,
                absoluteMag = h,
                orbitType = null
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convert MPC orbit to approximate current RA/Dec using mean elements.
     * This is simplified - for precise positions use JPL Horizons.
     */
    fun orbitToApproxPosition(orbit: MpcOrbit, jd: Double): Pair<Double, Double>? {
        if (orbit.semiMajorAxis <= 0 || orbit.eccentricity >= 1.0) return null

        val n = 0.9856076686 / (orbit.semiMajorAxis * sqrt(orbit.semiMajorAxis)) // deg/day
        val daysSinceEpoch = jd - orbit.epoch
        val m = Math.toRadians((orbit.meanAnomaly + n * daysSinceEpoch) % 360.0)

        // Solve Kepler
        var e = m
        for (i in 0..10) {
            val de = (e - orbit.eccentricity * sin(e) - m) / (1 - orbit.eccentricity * cos(e))
            e -= de
            if (kotlin.math.abs(de) < 1e-10) break
        }

        val trueAnomaly = 2.0 * atan2(
            sqrt(1 + orbit.eccentricity) * sin(e / 2),
            sqrt(1 - orbit.eccentricity) * cos(e / 2)
        )
        val r = orbit.semiMajorAxis * (1 - orbit.eccentricity * cos(e))

        val omega = Math.toRadians(orbit.argPerihelion)
        val node = Math.toRadians(orbit.longAscNode)
        val incl = Math.toRadians(orbit.inclination)

        val xHelio = r * (cos(node) * cos(omega + trueAnomaly) - sin(node) * sin(omega + trueAnomaly) * cos(incl))
        val yHelio = r * (sin(node) * cos(omega + trueAnomaly) + cos(node) * sin(omega + trueAnomaly) * cos(incl))
        val zHelio = r * sin(omega + trueAnomaly) * sin(incl)

        // Very simplified geocentric conversion (ignores Earth position properly)
        // For proper results, subtract Earth's heliocentric position
        val obliquity = Math.toRadians(23.439)
        val xEq = xHelio
        val yEq = yHelio * cos(obliquity) - zHelio * sin(obliquity)
        val zEq = yHelio * sin(obliquity) + zHelio * cos(obliquity)

        val ra = (atan2(yEq, xEq) * 12.0 / PI).let { if (it < 0) it + 24.0 else it }
        val dec = atan2(zEq, sqrt(xEq * xEq + yEq * yEq)) * 180.0 / PI

        return Pair(ra, dec)
    }

    fun toCelestialObjects(orbits: List<MpcOrbit>, jd: Double): List<CelestialObject> {
        return orbits.mapNotNull { orbit ->
            val pos = orbitToApproxPosition(orbit, jd) ?: return@mapNotNull null
            CelestialObject(
                id = "asteroid_${orbit.designation.replace(" ", "_")}",
                name = orbit.name ?: orbit.designation,
                catalogId = orbit.number?.let { "($it)" } ?: orbit.designation,
                type = ObjectType.ASTEROID,
                rightAscension = pos.first,
                declination = pos.second,
                magnitude = orbit.absoluteMag,
                description = orbit.orbitType
            )
        }
    }
}
