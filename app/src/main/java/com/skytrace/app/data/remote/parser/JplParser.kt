package com.skytrace.app.data.remote.parser

import com.google.gson.JsonParser
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType

/**
 * Parser for NASA JPL API responses.
 * Handles SBDB (Small-Body Database) and Horizons ephemeris output.
 */
object JplParser {

    data class SmallBody(
        val fullName: String,
        val designation: String?,
        val name: String?,
        val kind: String, // "an" for asteroid, "cn" for comet
        val orbitClass: String?,
        val epoch: Double?,
        val eccentricity: Double?,
        val semiMajorAxis: Double?,
        val inclination: Double?,
        val perihelionDist: Double?,
        val aphelionDist: Double?,
        val period: Double?, // orbital period (years)
        val absoluteMag: Double?,
        val diameter: Double? // km
    )

    data class CloseApproach(
        val designation: String,
        val date: String,
        val distanceAU: Double,
        val distanceLunar: Double,
        val velocity: Double, // km/s
        val absoluteMag: Double?,
        val diameter: String? // estimated range
    )

    data class EphemerisEntry(
        val datetime: String,
        val ra: Double, // degrees
        val dec: Double, // degrees
        val altitude: Double?,
        val azimuth: Double?,
        val magnitude: Double?,
        val delta: Double?, // distance AU
        val r: Double? // heliocentric distance AU
    )

    /**
     * Parse SBDB lookup response.
     */
    fun parseSmallBody(json: String): SmallBody? {
        return try {
            val root = JsonParser.parseString(json).asJsonObject

            val obj = root.getAsJsonObject("object") ?: return null
            val orbit = root.getAsJsonObject("orbit")
            val phys = root.getAsJsonObject("phys_par")
            val elements = orbit?.getAsJsonArray("elements")

            val fullName = obj.get("fullname")?.asString ?: obj.get("des")?.asString ?: ""
            val kind = obj.get("kind")?.asString ?: "an"

            fun getElement(name: String): Double? {
                elements?.forEach { el ->
                    val e = el.asJsonObject
                    if (e.get("name")?.asString == name) {
                        return e.get("value")?.asString?.toDoubleOrNull()
                    }
                }
                return null
            }

            SmallBody(
                fullName = fullName,
                designation = obj.get("des")?.asString,
                name = obj.get("name")?.asString,
                kind = kind,
                orbitClass = orbit?.getAsJsonObject("class")?.get("name")?.asString,
                epoch = getElement("epoch"),
                eccentricity = getElement("e"),
                semiMajorAxis = getElement("a"),
                inclination = getElement("i"),
                perihelionDist = getElement("q"),
                aphelionDist = getElement("ad"),
                period = getElement("per")?.let { it / 365.25 }, // convert days to years
                absoluteMag = phys?.getAsJsonArray("data")?.firstOrNull()
                    ?.asJsonObject?.get("value")?.asString?.toDoubleOrNull(),
                diameter = phys?.getAsJsonArray("data")?.find {
                    it.asJsonObject.get("name")?.asString == "diameter"
                }?.asJsonObject?.get("value")?.asString?.toDoubleOrNull()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Parse CAD (Close Approach Data) API response.
     */
    fun parseCloseApproaches(json: String): List<CloseApproach> {
        val results = mutableListOf<CloseApproach>()
        try {
            val root = JsonParser.parseString(json).asJsonObject
            val count = root.get("count")?.asInt ?: 0
            if (count == 0) return emptyList()

            val fields = root.getAsJsonArray("fields")?.map { it.asString } ?: return emptyList()
            val data = root.getAsJsonArray("data") ?: return emptyList()

            val desIdx = fields.indexOf("des")
            val dateIdx = fields.indexOf("cd")
            val distIdx = fields.indexOf("dist")
            val distMinIdx = fields.indexOf("dist_min")
            val vRelIdx = fields.indexOf("v_rel")
            val hIdx = fields.indexOf("h")

            for (row in data) {
                val arr = row.asJsonArray
                val dist = arr.get(distIdx)?.asString?.toDoubleOrNull() ?: continue

                results.add(
                    CloseApproach(
                        designation = arr.get(desIdx)?.asString ?: "",
                        date = arr.get(dateIdx)?.asString ?: "",
                        distanceAU = dist,
                        distanceLunar = dist * 389.17, // AU to lunar distances
                        velocity = arr.get(vRelIdx)?.asString?.toDoubleOrNull() ?: 0.0,
                        absoluteMag = if (hIdx >= 0) arr.get(hIdx)?.asString?.toDoubleOrNull() else null,
                        diameter = null
                    )
                )
            }
        } catch (e: Exception) {
            // Parse error
        }
        return results
    }

    /**
     * Parse Horizons ephemeris text response.
     * Horizons returns a text block with $$SOE / $$EOE markers.
     */
    fun parseEphemeris(response: String): List<EphemerisEntry> {
        val results = mutableListOf<EphemerisEntry>()
        try {
            // Extract JSON result
            val root = JsonParser.parseString(response).asJsonObject
            val resultStr = root.get("result")?.asString ?: return emptyList()

            val lines = resultStr.lines()
            var inData = false

            for (line in lines) {
                if (line.contains("\$\$SOE")) {
                    inData = true
                    continue
                }
                if (line.contains("\$\$EOE")) break
                if (!inData) continue
                if (line.isBlank()) continue

                // Parse ephemeris line (format depends on QUANTITIES requested)
                // Default: date, RA, DEC, APmag, delta, r
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size < 5) continue

                val datetime = "${parts[0]} ${parts[1]}"
                val ra = parseHmsToHours(parts.getOrNull(2), parts.getOrNull(3), parts.getOrNull(4))
                val dec = parseDmsToDegs(parts.getOrNull(5), parts.getOrNull(6), parts.getOrNull(7))

                if (ra != null && dec != null) {
                    results.add(
                        EphemerisEntry(
                            datetime = datetime,
                            ra = ra * 15.0, // hours to degrees
                            dec = dec,
                            altitude = null,
                            azimuth = null,
                            magnitude = parts.getOrNull(8)?.toDoubleOrNull(),
                            delta = parts.getOrNull(9)?.toDoubleOrNull(),
                            r = parts.getOrNull(10)?.toDoubleOrNull()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Parse error
        }
        return results
    }

    private fun parseHmsToHours(h: String?, m: String?, s: String?): Double? {
        val hours = h?.toDoubleOrNull() ?: return null
        val mins = m?.toDoubleOrNull() ?: 0.0
        val secs = s?.toDoubleOrNull() ?: 0.0
        return hours + mins / 60.0 + secs / 3600.0
    }

    private fun parseDmsToDegs(d: String?, m: String?, s: String?): Double? {
        val degs = d?.toDoubleOrNull() ?: return null
        val mins = m?.toDoubleOrNull() ?: 0.0
        val secs = s?.toDoubleOrNull() ?: 0.0
        val sign = if (degs < 0) -1.0 else 1.0
        return sign * (kotlin.math.abs(degs) + mins / 60.0 + secs / 3600.0)
    }

    fun smallBodyToCelestialObject(body: SmallBody): CelestialObject {
        val type = if (body.kind == "cn") ObjectType.COMET else ObjectType.ASTEROID
        return CelestialObject(
            id = "${type.name.lowercase()}_${body.designation?.replace(" ", "_") ?: body.fullName}",
            name = body.name ?: body.fullName,
            catalogId = body.designation,
            type = type,
            rightAscension = 0.0, // needs ephemeris calculation
            declination = 0.0,
            magnitude = body.absoluteMag,
            description = buildString {
                body.orbitClass?.let { append(it) }
                body.diameter?.let { append(" • Ø ${it}km") }
                body.period?.let { append(" • P=${String.format("%.2f", it)}yr") }
            }
        )
    }
}
