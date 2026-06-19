package com.skytrace.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.CollectionRepository
import com.skytrace.app.data.repository.SatelliteRepository
import com.skytrace.app.data.repository.SkyRepository
import com.skytrace.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val moonPhase: MoonPhase? = null,
    val visiblePlanets: List<CelestialObject> = emptyList(),
    val tonightObjects: List<CelestialObject> = emptyList(),
    val satellitePasses: List<SatellitePass> = emptyList(),
    val syncStatuses: List<SyncStatus> = emptyList(),
    val location: ObserverLocation? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val skyRepository: SkyRepository,
    private val satelliteRepository: SatelliteRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateLocation(location: ObserverLocation) {
        _uiState.value = _uiState.value.copy(location = location)
        loadData(location)
    }

    private fun loadData(location: ObserverLocation) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val moonPhase = skyRepository.getMoonPhase()
                val allObjects = skyRepository.getVisibleObjects(location, 6.0)
                val visiblePlanets = allObjects.filter { it.type == ObjectType.PLANET && it.isVisible }
                val tonight = skyRepository.getTonightBestObjects(location)

                _uiState.value = _uiState.value.copy(
                    moonPhase = moonPhase,
                    visiblePlanets = visiblePlanets,
                    tonightObjects = tonight,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load sky data: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        _uiState.value.location?.let { loadData(it) }
    }
}
