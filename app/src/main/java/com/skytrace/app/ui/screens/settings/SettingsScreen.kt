package com.skytrace.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.skytrace.app.ui.theme.*

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Settings", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location
        SettingsSection("Location") {
            SettingsRow("Use GPS Location", subtitle = "Automatic location detection") {
                Switch(
                    checked = uiState.useGps,
                    onCheckedChange = { viewModel.setUseGps(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
                )
            }
            if (!uiState.useGps) {
                SettingsRow("Latitude") {
                    Text("${uiState.manualLatitude}°", color = TextSecondary)
                }
                SettingsRow("Longitude") {
                    Text("${uiState.manualLongitude}°", color = TextSecondary)
                }
            }
        }

        // Display
        SettingsSection("Display") {
            SettingsRow("Night Mode", subtitle = "Red tint to preserve dark adaptation") {
                Switch(
                    checked = uiState.nightMode,
                    onCheckedChange = { viewModel.setNightMode(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = AccentBlue)
                )
            }
            SettingsRow("Magnitude Limit") {
                Text("${uiState.magnitudeLimit}", color = TextSecondary)
            }
            Slider(
                value = uiState.magnitudeLimit.toFloat(),
                onValueChange = { viewModel.setMagnitudeLimit(it.toDouble()) },
                valueRange = 2f..12f,
                steps = 9,
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue)
            )
        }

        // Data Sync
        SettingsSection("Data Sync") {
            SettingsRow(
                "Star Catalog",
                subtitle = "Built-in (${uiState.starCount} stars)"
            ) {
                Icon(Icons.Default.CheckCircle, "Synced", tint = Success, modifier = Modifier.size(20.dp))
            }
            SettingsRow(
                "Messier Catalog",
                subtitle = "Built-in (110 objects)"
            ) {
                Icon(Icons.Default.CheckCircle, "Synced", tint = Success, modifier = Modifier.size(20.dp))
            }
            SettingsRow(
                "Satellite TLE",
                subtitle = uiState.tleLastSync?.let { "Last sync: $it" } ?: "Never synced"
            ) {
                TextButton(onClick = { viewModel.syncSatellites() }) {
                    Text("Sync", color = AccentBlue, fontSize = 13.sp)
                }
            }
            SettingsRow(
                "MPC Asteroids",
                subtitle = uiState.mpcLastSync?.let { "Last sync: $it" } ?: "Never synced"
            ) {
                TextButton(onClick = { viewModel.syncMpc() }) {
                    Text("Sync", color = AccentBlue, fontSize = 13.sp)
                }
            }
        }

        // Export
        SettingsSection("Data") {
            SettingsRow("Export Observations (CSV)") {
                IconButton(onClick = { viewModel.exportCsv() }) {
                    Icon(Icons.Default.FileDownload, "Export", tint = AccentBlue)
                }
            }
            SettingsRow("Export Observations (JSON)") {
                IconButton(onClick = { viewModel.exportJson() }) {
                    Icon(Icons.Default.FileDownload, "Export", tint = AccentBlue)
                }
            }
            SettingsRow("Clear Cache", subtitle = "Remove downloaded catalog data") {
                IconButton(onClick = { viewModel.clearCache() }) {
                    Icon(Icons.Default.Delete, "Clear", tint = Error)
                }
            }
        }

        // Privacy
        SettingsSection("Privacy") {
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Privacy Information", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "SkyTrace stores all observation data locally on your device. " +
                                "No personal data is transmitted to external servers. " +
                                "Location data is used only for astronomical calculations and is never shared. " +
                                "Network requests are made only to download catalog data from public astronomy databases (CelesTrak, JPL, MPC).",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // About
        SettingsSection("About") {
            SettingsRow("Version") {
                Text("1.0.0", color = TextSecondary)
            }
            SettingsRow("Data Sources") {
                Text("MPC, JPL, CelesTrak", color = TextTertiary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        color = AccentBlue,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp)
            subtitle?.let {
                Text(it, color = TextTertiary, fontSize = 12.sp)
            }
        }
        trailing()
    }
}
