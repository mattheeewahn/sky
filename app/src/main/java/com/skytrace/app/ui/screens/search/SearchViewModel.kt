package com.skytrace.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.SkyRepository
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType
import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.services.sky.MessierCatalog
import com.skytrace.app.services.sky.NgcCatalog
import com.skytrace.app.services.sky.StarCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val typeFilter: ObjectType? = null,
    val results: List<CelestialObject> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val skyRepository: SkyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // Precomputed catalog for local search
    private val allObjects: List<CelestialObject> by lazy {
        val list = mutableListOf<CelestialObject>()
        list.addAll(StarCatalog.toCelestialObjects())
        list.addAll(MessierCatalog.toCelestialObjects())
        list.addAll(NgcCatalog.toCelestialObjects())
        // Planets
        list.add(CelestialObject("planet_mercury", "Mercury", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = -0.4))
        list.add(CelestialObject("planet_venus", "Venus", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = -4.4))
        list.add(CelestialObject("planet_mars", "Mars", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = -2.0))
        list.add(CelestialObject("planet_jupiter", "Jupiter", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = -2.7))
        list.add(CelestialObject("planet_saturn", "Saturn", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = 0.5))
        list.add(CelestialObject("planet_uranus", "Uranus", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = 5.7))
        list.add(CelestialObject("planet_neptune", "Neptune", type = ObjectType.PLANET, rightAscension = 0.0, declination = 0.0, magnitude = 7.8))
        list.add(CelestialObject("moon", "Moon", type = ObjectType.MOON, rightAscension = 0.0, declination = 0.0, magnitude = -12.7))
        list
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            performSearch(query, _uiState.value.typeFilter)
        }
    }

    fun toggleTypeFilter(type: ObjectType?) {
        _uiState.value = _uiState.value.copy(typeFilter = type)
        performSearch(_uiState.value.query, type)
    }

    private fun performSearch(query: String, typeFilter: ObjectType?) {
        if (query.isBlank() && typeFilter == null) {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
            return
        }

        _uiState.value = _uiState.value.copy(isSearching = true)

        val results = allObjects.filter { obj ->
            val matchesQuery = query.isBlank() ||
                    obj.name.contains(query, ignoreCase = true) ||
                    obj.catalogId?.contains(query, ignoreCase = true) == true ||
                    obj.constellation?.contains(query, ignoreCase = true) == true ||
                    obj.description?.contains(query, ignoreCase = true) == true

            val matchesType = typeFilter == null || obj.type == typeFilter

            matchesQuery && matchesType
        }.take(50)

        _uiState.value = _uiState.value.copy(results = results, isSearching = false)
    }
}
