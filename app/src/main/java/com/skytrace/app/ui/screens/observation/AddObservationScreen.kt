package com.skytrace.app.ui.screens.observation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skytrace.app.domain.model.*
import com.skytrace.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObservationScreen(
    navController: NavController,
    objectName: String = "",
    objectType: String = "",
    viewModel: AddObservationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(objectName, objectType) {
        if (objectName.isNotBlank()) {
            viewModel.prefill(objectName, objectType)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Add Observation", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
            TextButton(
                onClick = {
                    viewModel.save()
                    navController.popBackStack()
                },
                enabled = uiState.canSave
            ) {
                Text("Save", color = if (uiState.canSave) AccentBlue else TextTertiary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Object name
        FormField("Object Name *") {
            OutlinedTextField(
                value = uiState.objectName,
                onValueChange = { viewModel.updateField { copy(objectName = it) } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., M31, Saturn, Vega") },
                singleLine = true,
                colors = formFieldColors()
            )
        }

        // Catalog ID
        FormField("Catalog ID") {
            OutlinedTextField(
                value = uiState.catalogId,
                onValueChange = { viewModel.updateField { copy(catalogId = it) } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., NGC 224, HD 172167") },
                singleLine = true,
                colors = formFieldColors()
            )
        }

        // Object type dropdown
        FormField("Object Type") {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = uiState.selectedType.name,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = formFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ObjectType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                viewModel.updateField { copy(selectedType = type) }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Equipment section
        Text("Equipment", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        FormField("Telescope") {
            OutlinedTextField(
                value = uiState.telescope,
                onValueChange = { viewModel.updateField { copy(telescope = it) } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 8\" Dobsonian") },
                singleLine = true,
                colors = formFieldColors()
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormField("Eyepiece", Modifier.weight(1f)) {
                OutlinedTextField(
                    value = uiState.eyepiece,
                    onValueChange = { viewModel.updateField { copy(eyepiece = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 25mm") },
                    singleLine = true,
                    colors = formFieldColors()
                )
            }
            FormField("Filter", Modifier.weight(1f)) {
                OutlinedTextField(
                    value = uiState.filter,
                    onValueChange = { viewModel.updateField { copy(filter = it) } },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., OIII") },
                    singleLine = true,
                    colors = formFieldColors()
                )
            }
        }

        FormField("Camera") {
            OutlinedTextField(
                value = uiState.camera,
                onValueChange = { viewModel.updateField { copy(camera = it) } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., ZWO ASI294MC") },
                singleLine = true,
                colors = formFieldColors()
            )
        }

        FormField("Exposure (seconds)") {
            OutlinedTextField(
                value = uiState.exposure,
                onValueChange = { viewModel.updateField { copy(exposure = it) } },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 30") },
                singleLine = true,
                colors = formFieldColors()
            )
        }

        // Conditions section
        Text("Conditions", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        // Seeing
        FormField("Seeing") {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = uiState.seeing?.label ?: "Not specified",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = formFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SeeingCondition.entries.forEach { condition ->
                        DropdownMenuItem(
                            text = { Text("${condition.label} (${condition.arcSeconds})") },
                            onClick = {
                                viewModel.updateField { copy(seeing = condition) }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Transparency
        FormField("Transparency") {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = uiState.transparency?.label ?: "Not specified",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = formFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    Transparency.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.label) },
                            onClick = {
                                viewModel.updateField { copy(transparency = t) }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Sky brightness
        FormField("Sky Brightness (Bortle)") {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = uiState.skyBrightness?.let { "Class ${it.bortleClass} - ${it.label}" } ?: "Not specified",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = formFieldColors()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SkyBrightness.entries.forEach { sb ->
                        DropdownMenuItem(
                            text = { Text("Class ${sb.bortleClass} - ${sb.label}") },
                            onClick = {
                                viewModel.updateField { copy(skyBrightness = sb) }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        // Notes
        FormField("Notes") {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.updateField { copy(notes = it) } },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Observation notes...") },
                colors = formFieldColors()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun FormField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        content()
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = TextTertiary.copy(alpha = 0.5f),
    cursorColor = AccentBlue,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedPlaceholderColor = TextTertiary,
    unfocusedPlaceholderColor = TextTertiary
)
