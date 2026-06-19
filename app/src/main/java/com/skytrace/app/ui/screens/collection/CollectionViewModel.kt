package com.skytrace.app.ui.screens.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skytrace.app.data.repository.CollectionRepository
import com.skytrace.app.domain.model.CollectionEntry
import com.skytrace.app.domain.model.CollectionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionUiState(
    val entries: List<CollectionEntry> = emptyList(),
    val summary: CollectionSummary? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        loadCollection()
    }

    private fun loadCollection() {
        viewModelScope.launch {
            collectionRepository.getAll().collect { entries ->
                val summary = collectionRepository.getSummary()
                _uiState.value = CollectionUiState(
                    entries = entries,
                    summary = summary,
                    isLoading = false
                )
            }
        }
    }
}
