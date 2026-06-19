package com.skytrace.app.ui.screens.observation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.ObservationRepository
import com.skytrace.app.domain.model.Observation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ObservationLogUiState(
    val observations: List<Observation> = emptyList(),
    val isLoading: Boolean = true,
    val exportResult: String? = null
)

@HiltViewModel
class ObservationLogViewModel @Inject constructor(
    private val repository: ObservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ObservationLogUiState())
    val uiState: StateFlow<ObservationLogUiState> = _uiState.asStateFlow()

    init {
        loadObservations()
    }

    private fun loadObservations() {
        viewModelScope.launch {
            repository.getAllObservations().collect { observations ->
                _uiState.value = _uiState.value.copy(observations = observations, isLoading = false)
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                loadObservations()
            } else {
                repository.searchObservations(query).collect { results ->
                    _uiState.value = _uiState.value.copy(observations = results)
                }
            }
        }
    }

    fun delete(observation: Observation) {
        viewModelScope.launch {
            repository.delete(observation)
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            repository.exportToCsv().collect { csv ->
                _uiState.value = _uiState.value.copy(exportResult = csv)
            }
        }
    }
}
