package com.skytrace.app.data.repository

import com.skytrace.app.domain.model.*
import com.skytrace.app.services.sky.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for sky calculations - combines catalog data with
 * real-time position computation for the observer.
 */
@Singleton
class SkyRepository @Inject constructor() {

    /**
     * Get all visible objects for the observer at the current time.
     */
    fun getVisibleObjects(location: ObserverLocation, magnitudeLimit: Double = 6.0): List<CelestialObject> {
        val jd = AstronomyEngine.epochToJD(location.timestamp)
        val lstDeg = AstronomyEngine.lst(jd, location.longitude)

        val objects = mutableListOf<CelestialObject>()

        // Planets
        PlanetElements.ALL_PLANETS.forEach { planet ->
            val (ra, dec) = AstronomyEngine.planetPosition(planet, jd)
            val (alt, az) = AstronomyEngine.equatorialToHorizontal(ra, dec, location.latitude, lstDeg)
            objects.add(
                CelestialObject(
                    id = "planet_${planet.name.lowercase()}",
                    name = planet.name,
                    type = ObjectType.PLANET,
                    rightAscension = ra,
                    declination = dec,
                    magnitude = planet.magnitude,
                    altitude = alt,
                    azimuth = az,
                    isVisible = alt > 0
                )
            )
        }

        // Sun
        val (sunRa, sunDec) = AstronomyEngine.sunPosition(jd)
        val (sunAlt, sunAz) = AstronomyEngine.equatorialToHorizontal(sunRa, sunDec, location.latitude, lstDeg)
        objects.add(
            CelestialObject(
                id = "sun",
                name = "Sun",
                type = ObjectType.SUN,
                rightAscension = sunRa,
                declination = sunDec,
                magnitude = -26.74,
                altitude = sunAlt,
                azimuth = sunAz,
                isVisible = sunAlt > 0
            )
        )

        // Moon
        val (moonRa, moonDec) = AstronomyEngine.moonPosition(jd)
        val (moonAlt, moonAz) = AstronomyEngine.equatorialToHorizontal(moonRa, moonDec, location.latitude, lstDeg)
        objects.add(
            CelestialObject(
                id = "moon",
                name = "Moon",
                type = ObjectType.MOON,
                rightAscension = moonRa,
                declination = moonDec,
                magnitude = -12.7,
                altitude = moonAlt,
                azimuth = moonAz,
                isVisible = moonAlt > 0
            )
        )

        // Bright stars
        StarCatalog.brightStars
            .filter { it.magnitude <= magnitudeLimit }
            .forEach { star ->
                val (alt, az) = AstronomyEngine.equatorialToHorizontal(star.ra, star.dec, location.latitude, lstDeg)
                objects.add(
                    CelestialObject(
                        id = "star_${star.name.lowercase().replace(" ", "_")}",
                        name = star.name,
                        catalogId = star.bayer,
                        type = ObjectType.STAR,
                        rightAscension = star.ra,
                        declination = star.dec,
                        magnitude = star.magnitude,
                        altitude = alt,
                        azimuth = az,
                        isVisible = alt > 0,
                        constellation = star.constellation
                    )
                )
            }

        // Messier objects
        MessierCatalog.objects
            .filter { it.magnitude <= magnitudeLimit }
            .forEach { m ->
                val (alt, az) = AstronomyEngine.equatorialToHorizontal(m.ra, m.dec, location.latitude, lstDeg)
                objects.add(
                    CelestialObject(
                        id = "messier_${m.number}",
                        name = m.name?.let { "M${m.number} - $it" } ?: "M${m.number}",
                        catalogId = "M${m.number}",
                        type = ObjectType.MESSIER,
                        rightAscension = m.ra,
                        declination = m.dec,
                        magnitude = m.magnitude,
                        altitude = alt,
                        azimuth = az,
                        isVisible = alt > 0,
                        constellation = m.constellation,
                        description = m.type
                    )
                )
            }

        // NGC objects (built-in bright subset)
        NgcCatalog.objects
            .filter { it.magnitude <= magnitudeLimit }
            .forEach { ngc ->
                val (alt, az) = AstronomyEngine.equatorialToHorizontal(ngc.ra, ngc.dec, location.latitude, lstDeg)
                objects.add(
                    CelestialObject(
                        id = "ngc_${ngc.number}",
                        name = ngc.name?.let { "NGC ${ngc.number} - $it" } ?: "NGC ${ngc.number}",
                        catalogId = "NGC ${ngc.number}",
                        type = ObjectType.NGC,
                        rightAscension = ngc.ra,
                        declination = ngc.dec,
                        magnitude = ngc.magnitude,
                        altitude = alt,
                        azimuth = az,
                        isVisible = alt > 0,
                        constellation = ngc.constellation,
                        description = ngc.type
                    )
                )
            }

        return objects
    }

    /**
     * Get objects currently above the horizon, sorted by altitude.
     */
    fun getVisibleNow(location: ObserverLocation, magnitudeLimit: Double = 6.0): List<CelestialObject> =
        getVisibleObjects(location, magnitudeLimit).filter { it.isVisible }.sortedByDescending { it.altitude }

    /**
     * Get tonight's best objects (visible after sunset, high altitude).
     */
    fun getTonightBestObjects(location: ObserverLocation): List<CelestialObject> {
        val jd = AstronomyEngine.epochToJD(location.timestamp)
        val (sunRa, sunDec) = AstronomyEngine.sunPosition(jd)
        val lstDeg = AstronomyEngine.lst(jd, location.longitude)
        val (sunAlt, _) = AstronomyEngine.equatorialToHorizontal(sunRa, sunDec, location.latitude, lstDeg)

        // If sun is below -6 degrees, it's astronomical twilight or darker
        return if (sunAlt < -6.0) {
            getVisibleNow(location, 8.0).take(20)
        } else {
            // During daytime, compute for tonight
            getVisibleObjects(location, 6.0)
                .filter { it.altitude != null && it.altitude > 20 && it.type != ObjectType.SUN }
                .sortedByDescending { it.altitude }
                .take(20)
        }
    }

    fun getMoonPhase(epochMillis: Long = System.currentTimeMillis()): MoonPhase {
        val jd = AstronomyEngine.epochToJD(epochMillis)
        return AstronomyEngine.calculateMoonPhase(jd)
    }

    /**
     * Calculate position for a specific object at a given time.
     */
    fun calculatePosition(
        raHours: Double,
        decDeg: Double,
        location: ObserverLocation
    ): Pair<Double, Double> {
        val jd = AstronomyEngine.epochToJD(location.timestamp)
        val lstDeg = AstronomyEngine.lst(jd, location.longitude)
        return AstronomyEngine.equatorialToHorizontal(raHours, decDeg, location.latitude, lstDeg)
    }
}
