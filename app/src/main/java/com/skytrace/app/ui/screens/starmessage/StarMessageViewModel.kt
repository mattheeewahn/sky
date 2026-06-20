package com.skytrace.app.ui.screens.starmessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.StarMessageRepository
import com.skytrace.app.domain.model.*
import com.skytrace.app.services.sky.AstronomyEngine
import com.skytrace.app.services.sky.MessierCatalog
import com.skytrace.app.services.sky.StarCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StarMessageUiState(
    val step: MessageStep = MessageStep.SELECT_TARGET,
    val selectedTarget: CelestialObject? = null,
    val message: String = "",
    val senderName: String = "",
    val recipientName: String = "",
    val selectedTier: MessageTier = MessageTier.MESSAGE,
    val sentMessages: List<StarMessage> = emptyList(),
    val currentTransmission: StarMessage? = null,
    val transmissionProgress: Float = 0f,
    val isTransmitting: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<CelestialObject> = emptyList(),
    val totalSent: Int = 0
)

enum class MessageStep {
    SELECT_TARGET,
    COMPOSE,
    CONFIRM,
    TRANSMITTING,
    CERTIFICATE
}

@HiltViewModel
class StarMessageViewModel @Inject constructor(
    private val repository: StarMessageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StarMessageUiState())
    val uiState: StateFlow<StarMessageUiState> = _uiState.asStateFlow()

    private val popularTargets: List<CelestialObject> by lazy {
        listOf(
            CelestialObject("star_sirius", "Sirius", "α CMa", ObjectType.STAR, 6.752, -16.716, -1.46),
            CelestialObject("star_vega", "Vega", "α Lyr", ObjectType.STAR, 18.615, 38.783, 0.03),
            CelestialObject("star_polaris", "Polaris", "α UMi", ObjectType.STAR, 2.530, 89.264, 1.98),
            CelestialObject("star_betelgeuse", "Betelgeuse", "α Ori", ObjectType.STAR, 5.919, 7.407, 0.42),
            CelestialObject("messier_31", "Andromeda Galaxy", "M31", ObjectType.MESSIER, 0.712, 41.269, 3.4),
            CelestialObject("messier_42", "Orion Nebula", "M42", ObjectType.MESSIER, 5.588, -5.391, 4.0),
            CelestialObject("messier_45", "Pleiades", "M45", ObjectType.MESSIER, 3.787, 24.105, 1.6),
            CelestialObject("planet_mars", "Mars", null, ObjectType.PLANET, 0.0, 0.0, -2.0),
            CelestialObject("star_deneb", "Deneb", "α Cyg", ObjectType.STAR, 20.690, 45.280, 1.25),
            CelestialObject("messier_1", "Crab Nebula", "M1", ObjectType.MESSIER, 5.575, 22.015, 8.4)
        )
    }

    init {
        loadHistory()
        _uiState.value = _uiState.value.copy(searchResults = popularTargets)
    }

    private fun loadHistory() {
        viewModelScope.launch {
            repository.getAllMessages().collect { messages ->
                _uiState.value = _uiState.value.copy(
                    sentMessages = messages,
                    totalSent = messages.size
                )
            }
        }
    }

    fun searchTargets(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = popularTargets)
            return
        }

        val results = mutableListOf<CelestialObject>()

        // Search stars
        StarCatalog.brightStars
            .filter { it.name.contains(query, true) || it.bayer?.contains(query, true) == true }
            .take(5)
            .forEach { star ->
                results.add(
                    CelestialObject(
                        "star_${star.name.lowercase().replace(" ", "_")}", star.name, star.bayer,
                        ObjectType.STAR, star.ra, star.dec, star.magnitude, constellation = star.constellation
                    )
                )
            }

        // Search Messier
        MessierCatalog.objects
            .filter {
                "M${it.number}".contains(query, true) ||
                        it.name?.contains(query, true) == true
            }
            .take(5)
            .forEach { m ->
                results.add(
                    CelestialObject(
                        "messier_${m.number}", m.name?.let { "M${m.number} - $it" } ?: "M${m.number}",
                        "M${m.number}", ObjectType.MESSIER, m.ra, m.dec, m.magnitude,
                        constellation = m.constellation, description = m.type
                    )
                )
            }

        // Planets
        listOf("Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune")
            .filter { it.contains(query, true) }
            .forEach { name ->
                results.add(
                    CelestialObject("planet_${name.lowercase()}", name, null, ObjectType.PLANET, 0.0, 0.0, null)
                )
            }

        _uiState.value = _uiState.value.copy(searchResults = results.take(10))
    }

    fun selectTarget(target: CelestialObject) {
        _uiState.value = _uiState.value.copy(selectedTarget = target, step = MessageStep.COMPOSE)
    }

    fun updateMessage(msg: String) {
        val tier = _uiState.value.selectedTier
        _uiState.value = _uiState.value.copy(message = msg.take(tier.maxChars))
    }

    fun updateSenderName(name: String) {
        _uiState.value = _uiState.value.copy(senderName = name)
    }

    fun updateRecipientName(name: String) {
        _uiState.value = _uiState.value.copy(recipientName = name)
    }

    fun selectTier(tier: MessageTier) {
        _uiState.value = _uiState.value.copy(
            selectedTier = tier,
            message = _uiState.value.message.take(tier.maxChars)
        )
    }

    fun goToConfirm() {
        _uiState.value = _uiState.value.copy(step = MessageStep.CONFIRM)
    }

    fun goBack() {
        val newStep = when (_uiState.value.step) {
            MessageStep.COMPOSE -> MessageStep.SELECT_TARGET
            MessageStep.CONFIRM -> MessageStep.COMPOSE
            MessageStep.CERTIFICATE -> MessageStep.SELECT_TARGET
            else -> _uiState.value.step
        }
        _uiState.value = _uiState.value.copy(step = newStep)
    }

    /**
     * Simulate the transmission process with animations.
     * In production, this would trigger real RF hardware via backend API.
     */
    fun transmit() {
        val state = _uiState.value
        val target = state.selectedTarget ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(step = MessageStep.TRANSMITTING, isTransmitting = true, transmissionProgress = 0f)

            // Save message
            val sentMessage = repository.sendMessage(
                target = target,
                message = state.message,
                senderName = state.senderName,
                recipientName = state.recipientName.ifBlank { null },
                tier = state.selectedTier
            )

            // Simulate transmission phases
            // Phase 1: Encoding
            for (i in 0..20) {
                delay(50)
                _uiState.value = _uiState.value.copy(transmissionProgress = i / 100f)
            }

            repository.updateStatus(sentMessage.id, TransmissionStatus.TRANSMITTING)

            // Phase 2: Transmitting
            for (i in 21..80) {
                delay(40)
                _uiState.value = _uiState.value.copy(transmissionProgress = i / 100f)
            }

            // Phase 3: Confirmation
            for (i in 81..100) {
                delay(30)
                _uiState.value = _uiState.value.copy(transmissionProgress = i / 100f)
            }

            repository.updateStatus(sentMessage.id, TransmissionStatus.TRAVELING)

            _uiState.value = _uiState.value.copy(
                step = MessageStep.CERTIFICATE,
                currentTransmission = sentMessage.copy(status = TransmissionStatus.TRAVELING),
                isTransmitting = false
            )
        }
    }

    fun reset() {
        _uiState.value = StarMessageUiState(
            sentMessages = _uiState.value.sentMessages,
            totalSent = _uiState.value.totalSent,
            searchResults = popularTargets
        )
    }
}
