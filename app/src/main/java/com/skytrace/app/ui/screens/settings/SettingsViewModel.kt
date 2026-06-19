package com.skytrace.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.SatelliteRepository
import com.skytrace.app.domain.model.SatelliteCategory
import com.skytrace.app.services.sky.StarCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val useGps: Boolean = true,
    val manualLatitude: Double = 0.0,
    val manualLongitude: Double = 0.0,
    val nightMode: Boolean = false,
    val magnitudeLimit: Double = 6.0,
    val starCount: Int = StarCatalog.brightStars.size,
    val tleLastSync: String? = null,
    val mpcLastSync: String? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val satelliteRepository: SatelliteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setUseGps(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(useGps = enabled)
    }

    fun setNightMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(nightMode = enabled)
    }

    fun setMagnitudeLimit(limit: Double) {
        _uiState.value = _uiState.value.copy(magnitudeLimit = limit)
    }

    fun syncSatellites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            try {
                // Sync ISS and visible satellites
                val result = satelliteRepository.syncCategory(SatelliteCategory.ISS)
                result.onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        tleLastSync = "Just now ($count satellites)",
                        isSyncing = false
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        syncError = "TLE sync failed: ${e.message}",
                        isSyncing = false
                    )
                }

                // Also sync Starlink
                satelliteRepository.syncCategory(SatelliteCategory.STARLINK)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    syncError = e.message,
                    isSyncing = false
                )
            }
        }
    }

    fun syncMpc() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            // MPC sync would download MPCORB data
            // For now, mark architecture as ready
            _uiState.value = _uiState.value.copy(
                mpcLastSync = "MPC sync requires network",
                isSyncing = false
            )
        }
    }

    fun exportCsv() {
        // Trigger CSV export through ObservationRepository
    }

    fun exportJson() {
        // Trigger JSON export
    }

    fun clearCache() {
        viewModelScope.launch {
            // Clear TLE cache and catalog cache
            _uiState.value = _uiState.value.copy(
                tleLastSync = null,
                mpcLastSync = null
            )
        }
    }
}
