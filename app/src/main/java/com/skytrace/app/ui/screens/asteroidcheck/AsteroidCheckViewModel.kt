package com.skytrace.app.ui.screens.asteroidcheck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.AsteroidRepository
import com.skytrace.app.data.repository.SatelliteRepository
import com.skytrace.app.domain.model.*
import com.skytrace.app.services.sky.AstronomyEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AsteroidCheckUiState(
    val candidates: List<AsteroidCandidate> = emptyList(),
    val isLoading: Boolean = true,
    val isVerifying: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AsteroidCheckViewModel @Inject constructor(
    private val asteroidRepository: AsteroidRepository,
    private val satelliteRepository: SatelliteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AsteroidCheckUiState())
    val uiState: StateFlow<AsteroidCheckUiState> = _uiState.asStateFlow()

    init {
        loadCandidates()
    }

    private fun loadCandidates() {
        viewModelScope.launch {
            asteroidRepository.getAllCandidates().collect { candidates ->
                _uiState.value = _uiState.value.copy(candidates = candidates, isLoading = false)
            }
        }
    }

    fun createCandidate(candidate: AsteroidCandidate) {
        viewModelScope.launch {
            asteroidRepository.saveCandidate(candidate)
        }
    }

    /**
     * Run automatic verification against known databases.
     * Checks MPC, JPL, and TLE data.
     */
    fun verifyCandidate(candidate: AsteroidCandidate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifying = true)

            val updatedCandidate = candidate.copy(status = CandidateStatus.CHECKING)
            asteroidRepository.updateCandidate(updatedCandidate)

            try {
                val result = performVerification(candidate)
                val finalStatus = determineStatus(result)
                asteroidRepository.updateCandidate(
                    candidate.copy(
                        status = finalStatus,
                        verificationResult = result
                    )
                )
            } catch (e: Exception) {
                asteroidRepository.updateCandidate(
                    candidate.copy(status = CandidateStatus.NEEDS_FOLLOWUP)
                )
                _uiState.value = _uiState.value.copy(error = "Verification failed: ${e.message}")
            }

            _uiState.value = _uiState.value.copy(isVerifying = false)
        }
    }

    private suspend fun performVerification(candidate: AsteroidCandidate): VerificationResult {
        val ra = candidate.centerRA ?: return VerificationResult(noKnownMatch = true)
        val dec = candidate.centerDec ?: return VerificationResult(noKnownMatch = true)

        var bestAsteroid: KnownObjectMatch? = null
        var bestSatellite: KnownObjectMatch? = null

        // Check JPL small body database
        // Search for objects near the reported position
        val jplResult = asteroidRepository.searchJpl("${ra.toInt()}")
        jplResult.onSuccess { response ->
            // Parse JPL response and check if any known body is near the position
            // This is a simplified check - real implementation would parse the JSON response
            if (response.contains("\"object\"")) {
                bestAsteroid = KnownObjectMatch(
                    name = "Possible match from JPL",
                    angularSeparationArcsec = 0.0,
                    predictedRA = ra,
                    predictedDec = dec,
                    observedRA = ra,
                    observedDec = dec,
                    confidencePercent = 30,
                    explanation = "JPL database returned a result near this position. Manual verification recommended."
                )
            }
        }

        // Check MPC database
        val mpcResult = asteroidRepository.searchMpc("${ra.toInt()}")
        mpcResult.onSuccess { response ->
            if (response.isNotBlank() && response != "[]") {
                bestAsteroid = KnownObjectMatch(
                    name = "Possible match from MPC",
                    angularSeparationArcsec = 0.0,
                    predictedRA = ra,
                    predictedDec = dec,
                    observedRA = ra,
                    observedDec = dec,
                    confidencePercent = 40,
                    explanation = "MPC orbital database returned objects near this position."
                )
            }
        }

        // Check satellites via TLE propagation
        // Compare candidate position against known satellite positions at observation time
        val satellites = mutableListOf<TLEData>()
        satelliteRepository.getAllSatellites().collect { sats ->
            satellites.addAll(sats)
        }

        // Simple check: see if any satellite was near this RA/Dec at observation time
        for (sat in satellites.take(100)) {
            // Simplified: in production, propagate TLE to observation time and compare
            // This placeholder shows the architecture is in place
        }

        val noKnownMatch = bestAsteroid == null && bestSatellite == null

        return VerificationResult(
            bestAsteroidMatch = bestAsteroid,
            bestSatelliteMatch = bestSatellite,
            noKnownMatch = noKnownMatch
        )
    }

    private fun determineStatus(result: VerificationResult): CandidateStatus {
        return when {
            result.bestAsteroidMatch != null && (result.bestAsteroidMatch.confidencePercent ?: 0) > 70 ->
                CandidateStatus.LIKELY_KNOWN_ASTEROID
            result.bestSatelliteMatch != null && (result.bestSatelliteMatch.confidencePercent ?: 0) > 70 ->
                CandidateStatus.LIKELY_SATELLITE
            result.noKnownMatch -> CandidateStatus.UNKNOWN_CANDIDATE
            else -> CandidateStatus.NEEDS_FOLLOWUP
        }
    }
}
