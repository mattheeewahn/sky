package com.skytrace.app.ui.screens.telescope

import androidx.lifecycle.ViewModel
import com.skytrace.app.data.repository.SkyRepository
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType
import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.services.sky.AstronomyEngine
import com.skytrace.app.services.sky.MessierCatalog
import com.skytrace.app.services.sky.PlanetElements
import com.skytrace.app.services.sky.StarCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TelescopePointingUiState(
    val targetObject: CelestialObject? = null,
    val targetAzimuth: Double? = null,
    val targetAltitude: Double? = null,
    val phoneAzimuth: Float = 0f,
    val phoneAltitude: Float = 0f,
    val azimuthDifference: Float = 0f,
    val altitudeDifference: Float = 0f
)

@HiltViewModel
class TelescopePointingViewModel @Inject constructor(
    private val skyRepository: SkyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelescopePointingUiState())
    val uiState: StateFlow<TelescopePointingUiState> = _uiState.asStateFlow()

    fun loadObject(objectId: String) {
        // Resolve object from ID and calculate current position
        val obj = resolveObject(objectId)
        obj?.let {
            _uiState.value = _uiState.value.copy(
                targetObject = it,
                targetAzimuth = it.azimuth,
                targetAltitude = it.altitude
            )
        }
    }

    fun updatePhoneDirection(azimuth: Float, altitude: Float) {
        val state = _uiState.value
        val targetAz = state.targetAzimuth ?: return
        val targetAlt = state.targetAltitude ?: return

        var azDiff = (targetAz - azimuth).toFloat()
        if (azDiff > 180) azDiff -= 360
        if (azDiff < -180) azDiff += 360

        val altDiff = (targetAlt - altitude).toFloat()

        _uiState.value = state.copy(
            phoneAzimuth = azimuth,
            phoneAltitude = altitude,
            azimuthDifference = azDiff,
            altitudeDifference = altDiff
        )
    }

    /**
     * Resolve an object by its ID and compute its current alt/az.
     * Uses a default observer location if none available.
     */
    private fun resolveObject(objectId: String): CelestialObject? {
        val now = System.currentTimeMillis()
        val jd = AstronomyEngine.epochToJD(now)

        // Default location (will be updated when GPS is available)
        val defaultLat = 40.0
        val defaultLon = -74.0
        val lstDeg = AstronomyEngine.lst(jd, defaultLon)

        return when {
            objectId.startsWith("planet_") -> {
                val name = objectId.removePrefix("planet_")
                val elements = PlanetElements.ALL_PLANETS.find { it.name.lowercase() == name }
                elements?.let {
                    val (ra, dec) = AstronomyEngine.planetPosition(it, jd)
                    val (alt, az) = AstronomyEngine.equatorialToHorizontal(ra, dec, defaultLat, lstDeg)
                    CelestialObject(
                        id = objectId, name = it.name, type = ObjectType.PLANET,
                        rightAscension = ra, declination = dec,
                        altitude = alt, azimuth = az, magnitude = it.magnitude, isVisible = alt > 0
                    )
                }
            }
            objectId == "moon" -> {
                val (ra, dec) = AstronomyEngine.moonPosition(jd)
                val (alt, az) = AstronomyEngine.equatorialToHorizontal(ra, dec, defaultLat, lstDeg)
                CelestialObject(
                    id = "moon", name = "Moon", type = ObjectType.MOON,
                    rightAscension = ra, declination = dec,
                    altitude = alt, azimuth = az, magnitude = -12.7, isVisible = alt > 0
                )
            }
            objectId.startsWith("messier_") -> {
                val num = objectId.removePrefix("messier_").toIntOrNull() ?: return null
                val m = MessierCatalog.objects.find { it.number == num } ?: return null
                val (alt, az) = AstronomyEngine.equatorialToHorizontal(m.ra, m.dec, defaultLat, lstDeg)
                CelestialObject(
                    id = objectId, name = m.name?.let { "M$num - $it" } ?: "M$num",
                    catalogId = "M$num", type = ObjectType.MESSIER,
                    rightAscension = m.ra, declination = m.dec,
                    altitude = alt, azimuth = az, magnitude = m.magnitude, isVisible = alt > 0,
                    constellation = m.constellation, description = m.type
                )
            }
            objectId.startsWith("star_") -> {
                val name = objectId.removePrefix("star_").replace("_", " ")
                val star = StarCatalog.brightStars.find { it.name.lowercase() == name }
                star?.let {
                    val (alt, az) = AstronomyEngine.equatorialToHorizontal(it.ra, it.dec, defaultLat, lstDeg)
                    CelestialObject(
                        id = objectId, name = it.name, catalogId = it.bayer,
                        type = ObjectType.STAR, rightAscension = it.ra, declination = it.dec,
                        altitude = alt, azimuth = az, magnitude = it.magnitude, isVisible = alt > 0,
                        constellation = it.constellation
                    )
                }
            }
            else -> null
        }
    }
}
