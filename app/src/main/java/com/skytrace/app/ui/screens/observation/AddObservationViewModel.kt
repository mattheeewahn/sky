package com.skytrace.app.ui.screens.observation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.CollectionRepository
import com.skytrace.app.data.repository.ObservationRepository
import com.skytrace.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddObservationUiState(
    val objectName: String = "",
    val catalogId: String = "",
    val selectedType: ObjectType = ObjectType.STAR,
    val telescope: String = "",
    val eyepiece: String = "",
    val camera: String = "",
    val filter: String = "",
    val exposure: String = "",
    val seeing: SeeingCondition? = null,
    val transparency: Transparency? = null,
    val skyBrightness: SkyBrightness? = null,
    val notes: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val canSave: Boolean get() = objectName.isNotBlank()
}

@HiltViewModel
class AddObservationViewModel @Inject constructor(
    private val observationRepository: ObservationRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddObservationUiState())
    val uiState: StateFlow<AddObservationUiState> = _uiState.asStateFlow()

    fun prefill(objectName: String, objectType: String) {
        val type = try { ObjectType.valueOf(objectType) } catch (e: Exception) { ObjectType.STAR }
        _uiState.value = _uiState.value.copy(objectName = objectName, selectedType = type)
    }

    fun updateField(update: AddObservationUiState.() -> AddObservationUiState) {
        _uiState.value = _uiState.value.update()
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            val observation = Observation(
                objectName = state.objectName,
                catalogId = state.catalogId.ifBlank { null },
                objectType = state.selectedType,
                dateTime = System.currentTimeMillis(),
                latitude = state.latitude,
                longitude = state.longitude,
                telescope = state.telescope.ifBlank { null },
                eyepiece = state.eyepiece.ifBlank { null },
                camera = state.camera.ifBlank { null },
                filter = state.filter.ifBlank { null },
                exposureSeconds = state.exposure.toDoubleOrNull(),
                seeingCondition = state.seeing,
                transparency = state.transparency,
                skyBrightness = state.skyBrightness,
                notes = state.notes.ifBlank { null }
            )

            observationRepository.save(observation)

            // Also add to collection
            collectionRepository.addOrUpdate(
                objectName = state.objectName,
                catalogId = state.catalogId.ifBlank { null },
                objectType = state.selectedType
            )

            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
