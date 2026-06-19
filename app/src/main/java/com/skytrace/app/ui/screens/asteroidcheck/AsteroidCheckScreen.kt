package com.skytrace.app.ui.screens.asteroidcheck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.skytrace.app.domain.model.AsteroidCandidate
import com.skytrace.app.domain.model.CandidateStatus
import com.skytrace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsteroidCheckScreen(
    navController: NavController,
    viewModel: AsteroidCheckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
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
                Text("Asteroid Check", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
            FloatingActionButton(
                onClick = { showAddForm = true },
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, "Add Candidate")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Disclaimer
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, "Info", tint = Warning, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "This app cannot confirm an official asteroid discovery. Official discovery requires follow-up observations and submission to the Minor Planet Center.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showAddForm) {
            // New candidate form
            NewCandidateForm(
                onSave = { viewModel.createCandidate(it); showAddForm = false },
                onCancel = { showAddForm = false }
            )
        } else {
            // Candidate list
            if (uiState.candidates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Radar, "No candidates", tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No asteroid candidates", color = TextSecondary)
                        Text("Tap + to report a suspected moving object", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.candidates) { candidate ->
                        CandidateRow(candidate) {
                            viewModel.verifyCandidate(candidate)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateRow(candidate: AsteroidCandidate, onVerify: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val statusColor = when (candidate.status) {
        CandidateStatus.DRAFT -> TextSecondary
        CandidateStatus.CHECKING -> Warning
        CandidateStatus.UNKNOWN_CANDIDATE -> AccentBlue
        CandidateStatus.NEEDS_FOLLOWUP -> Warning
        CandidateStatus.LIKELY_KNOWN_ASTEROID -> Success
        else -> TextTertiary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Candidate #${candidate.id}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    candidate.status.label,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                dateFormat.format(Date(candidate.observationTime)),
                color = TextTertiary,
                fontSize = 12.sp
            )
            candidate.centerRA?.let { ra ->
                candidate.centerDec?.let { dec ->
                    Text(
                        "RA: ${String.format("%.4f", ra)}h Dec: ${String.format("%.2f", dec)}°",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            candidate.telescope?.let {
                Text("Telescope: $it", color = TextTertiary, fontSize = 11.sp)
            }

            // Verification result
            candidate.verificationResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                result.bestAsteroidMatch?.let { match ->
                    Text("Closest asteroid: ${match.name}", color = Success, fontSize = 12.sp)
                    Text(
                        "Separation: ${String.format("%.1f", match.angularSeparationArcsec)}\" | Confidence: ${match.confidencePercent}%",
                        color = TextSecondary, fontSize = 11.sp
                    )
                }
                result.bestSatelliteMatch?.let { match ->
                    Text("Possible satellite: ${match.name}", color = Warning, fontSize = 12.sp)
                }
                if (result.noKnownMatch) {
                    Text("No known match found", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (candidate.status == CandidateStatus.DRAFT || candidate.status == CandidateStatus.NEEDS_FOLLOWUP) {
                    OutlinedButton(onClick = onVerify, modifier = Modifier.height(32.dp)) {
                        Text("Verify", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NewCandidateForm(
    onSave: (AsteroidCandidate) -> Unit,
    onCancel: () -> Unit
) {
    var centerRA by remember { mutableStateOf("") }
    var centerDec by remember { mutableStateOf("") }
    var telescope by remember { mutableStateOf("") }
    var camera by remember { mutableStateOf("") }
    var exposure by remember { mutableStateOf("") }
    var fov by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("New Asteroid Candidate", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = centerRA, onValueChange = { centerRA = it },
            label = { Text("Center RA (hours)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = centerDec, onValueChange = { centerDec = it },
            label = { Text("Center Dec (degrees)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = telescope, onValueChange = { telescope = it },
            label = { Text("Telescope") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = camera, onValueChange = { camera = it },
            label = { Text("Camera") },
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = exposure, onValueChange = { exposure = it },
                label = { Text("Exposure (s)") },
                modifier = Modifier.weight(1f), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                    cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
            OutlinedTextField(
                value = fov, onValueChange = { fov = it },
                label = { Text("FOV (arcmin)") },
                modifier = Modifier.weight(1f), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                    cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    val candidate = AsteroidCandidate(
                        observationTime = System.currentTimeMillis(),
                        latitude = 0.0, // TODO: get from GPS
                        longitude = 0.0,
                        telescope = telescope.ifBlank { null },
                        camera = camera.ifBlank { null },
                        exposureSeconds = exposure.toDoubleOrNull(),
                        fieldOfViewArcmin = fov.toDoubleOrNull(),
                        centerRA = centerRA.toDoubleOrNull(),
                        centerDec = centerDec.toDoubleOrNull(),
                        notes = notes.ifBlank { null }
                    )
                    onSave(candidate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Candidate")
            }
        }
    }
}
