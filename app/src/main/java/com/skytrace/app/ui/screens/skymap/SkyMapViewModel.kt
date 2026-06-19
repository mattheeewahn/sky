package com.skytrace.app.ui.screens.skymap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.SkyRepository
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType
import com.skytrace.app.domain.model.ObserverLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkyMapUiState(
    val objects: List<CelestialObject> = emptyList(),
    val filteredObjects: List<CelestialObject> = emptyList(),
    val selectedObject: CelestialObject? = null,
    val location: ObserverLocation? = null,
    val magnitudeLimit: Double = 6.0,
    val showStars: Boolean = true,
    val showPlanets: Boolean = true,
    val showMessier: Boolean = true,
    val showNGC: Boolean = false,
    val showSatellites: Boolean = false,
    val showAsteroids: Boolean = false,
    val searchQuery: String = "",
    val phoneAzimuth: Float = 0f,
    val phoneAltitude: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class SkyMapViewModel @Inject constructor(
    private val skyRepository: SkyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkyMapUiState())
    val uiState: StateFlow<SkyMapUiState> = _uiState.asStateFlow()

    fun updateLocation(location: ObserverLocation) {
        _uiState.value = _uiState.value.copy(location = location)
        loadObjects(location)
    }

    fun updateSensorData(azimuth: Float, altitude: Float) {
        _uiState.value = _uiState.value.copy(phoneAzimuth = azimuth, phoneAltitude = altitude)
    }

    fun selectObject(obj: CelestialObject?) {
        _uiState.value = _uiState.value.copy(selectedObject = obj)
    }

    fun setMagnitudeLimit(limit: Double) {
        _uiState.value = _uiState.value.copy(magnitudeLimit = limit)
        applyFilters()
    }

    fun toggleFilter(type: ObjectType, enabled: Boolean) {
        _uiState.value = when (type) {
            ObjectType.STAR -> _uiState.value.copy(showStars = enabled)
            ObjectType.PLANET -> _uiState.value.copy(showPlanets = enabled)
            ObjectType.MESSIER -> _uiState.value.copy(showMessier = enabled)
            ObjectType.NGC -> _uiState.value.copy(showNGC = enabled)
            ObjectType.SATELLITE -> _uiState.value.copy(showSatellites = enabled)
            ObjectType.ASTEROID -> _uiState.value.copy(showAsteroids = enabled)
            else -> _uiState.value
        }
        applyFilters()
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    private fun loadObjects(location: ObserverLocation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val objects = skyRepository.getVisibleObjects(location, _uiState.value.magnitudeLimit)
            _uiState.value = _uiState.value.copy(objects = objects, isLoading = false)
            applyFilters()
        }
    }

    private fun applyFilters() {
        val state = _uiState.value
        val filtered = state.objects.filter { obj ->
            val typeMatch = when (obj.type) {
                ObjectType.STAR -> state.showStars
                ObjectType.PLANET -> state.showPlanets
                ObjectType.MOON, ObjectType.SUN -> state.showPlanets
                ObjectType.MESSIER -> state.showMessier
                ObjectType.NGC -> state.showNGC
                ObjectType.SATELLITE -> state.showSatellites
                ObjectType.ASTEROID -> state.showAsteroids
                else -> true
            }
            val magMatch = obj.magnitude == null || obj.magnitude <= state.magnitudeLimit
            val searchMatch = state.searchQuery.isBlank() ||
                    obj.name.contains(state.searchQuery, ignoreCase = true) ||
                    obj.catalogId?.contains(state.searchQuery, ignoreCase = true) == true
            typeMatch && magMatch && searchMatch && obj.isVisible
        }
        _uiState.value = state.copy(filteredObjects = filtered)
    }
}
