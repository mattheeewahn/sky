package com.skytrace.app.ui.screens.blink

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BlinkUiState(
    val images: List<String> = emptyList(), // URIs
    val currentFrame: Int = 0,
    val isPlaying: Boolean = false,
    val blinkSpeedMs: Int = 500,
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val markedX: Float? = null,
    val markedY: Float? = null,
    val estimatedMovement: String? = null
)

@HiltViewModel
class BlinkViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BlinkUiState())
    val uiState: StateFlow<BlinkUiState> = _uiState.asStateFlow()

    fun addImage() {
        // In production, this would open a file picker.
        // The URI would be added to the images list.
        // For now, the architecture is in place.
    }

    fun addImageUri(uri: String) {
        val current = _uiState.value.images.toMutableList()
        current.add(uri)
        _uiState.value = _uiState.value.copy(images = current)
    }

    fun removeImage(index: Int) {
        val current = _uiState.value.images.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(
                images = current,
                currentFrame = _uiState.value.currentFrame.coerceIn(0, (current.size - 1).coerceAtLeast(0))
            )
        }
    }

    fun nextFrame() {
        val state = _uiState.value
        if (state.images.isEmpty()) return
        _uiState.value = state.copy(
            currentFrame = (state.currentFrame + 1) % state.images.size
        )
    }

    fun previousFrame() {
        val state = _uiState.value
        if (state.images.isEmpty()) return
        _uiState.value = state.copy(
            currentFrame = if (state.currentFrame > 0) state.currentFrame - 1 else state.images.size - 1
        )
    }

    fun togglePlay() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun setBlinkSpeed(ms: Int) {
        _uiState.value = _uiState.value.copy(blinkSpeedMs = ms.coerceIn(100, 2000))
    }

    fun setBrightness(value: Float) {
        _uiState.value = _uiState.value.copy(brightness = value)
    }

    fun setContrast(value: Float) {
        _uiState.value = _uiState.value.copy(contrast = value)
    }

    fun markPosition(x: Float, y: Float) {
        _uiState.value = _uiState.value.copy(markedX = x, markedY = y)
    }

    fun clearMark() {
        _uiState.value = _uiState.value.copy(markedX = null, markedY = null)
    }
}
