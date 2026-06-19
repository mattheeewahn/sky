package com.skytrace.app.ui.screens.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.MoonPhase
import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.ui.navigation.Screen
import com.skytrace.app.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateLocation(
                            ObserverLocation(it.latitude, it.longitude, it.altitude)
                        )
                    } ?: run {
                        // No last known location, use default
                        viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
                    }
                }.addOnFailureListener {
                    viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
                }
            } catch (e: Exception) {
                viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
            }
        } else {
            // Load with default location (Seoul) before permission
            viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
            locationPermission.launchPermissionRequest()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SkyTrace",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
        }

        // Moon phase card
        item {
            uiState.moonPhase?.let { MoonPhaseCard(it) }
        }

        // Quick actions
        item {
            QuickActionsRow(navController)
        }

        // Visible planets
        if (uiState.visiblePlanets.isNotEmpty()) {
            item {
                SectionHeader("Visible Planets")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.visiblePlanets) { planet ->
                        PlanetChip(planet)
                    }
                }
            }
        }

        // Tonight's objects
        if (uiState.tonightObjects.isNotEmpty()) {
            item {
                SectionHeader("Tonight's Objects")
            }
            items(uiState.tonightObjects.take(10)) { obj ->
                ObjectRow(obj, onClick = {
                    navController.navigate("telescope/${obj.id}")
                })
            }
        }

        // Loading / Error states
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
        }

        uiState.error?.let { error ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = Error,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Location not available
        if (!locationPermission.status.isGranted && !uiState.isLoading) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Location Required", color = Warning, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "SkyTrace needs your location to calculate accurate positions of celestial objects.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { locationPermission.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoonPhaseCard(moonPhase: MoonPhase) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = moonPhase.emoji,
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(moonPhase.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    "Illumination: ${(moonPhase.illumination * 100).toInt()}%",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    "Age: ${moonPhase.age.toInt()} days",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun QuickActionsRow(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton("Sky Map", Icons.Default.Map, Modifier.weight(1f)) {
            navController.navigate(Screen.SkyMap.route)
        }
        QuickActionButton("Search", Icons.Default.Search, Modifier.weight(1f)) {
            navController.navigate(Screen.Search.route)
        }
        QuickActionButton("Asteroid", Icons.Default.Radar, Modifier.weight(1f)) {
            navController.navigate(Screen.AsteroidCheck.route)
        }
        QuickActionButton("Log", Icons.Default.NoteAlt, Modifier.weight(1f)) {
            navController.navigate(Screen.ObservationLog.route)
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = AccentBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun PlanetChip(planet: CelestialObject) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(planet.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Alt: ${planet.altitude?.toInt() ?: 0}°",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                "Mag: ${planet.magnitude ?: "?"}",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ObjectRow(obj: CelestialObject, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(obj.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row {
                    obj.catalogId?.let {
                        Text(it, color = AccentBlue, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(obj.type.name, color = TextTertiary, fontSize = 12.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Alt ${obj.altitude?.toInt() ?: 0}°",
                    color = if ((obj.altitude ?: 0.0) > 30) Success else TextSecondary,
                    fontSize = 13.sp
                )
                obj.magnitude?.let {
                    Text("Mag ${String.format("%.1f", it)}", color = TextTertiary, fontSize = 11.sp)
                }
            }
        }
    }
}
