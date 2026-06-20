package com.skytrace.app.ui.screens.skymap

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
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
import com.skytrace.app.domain.model.ObjectType
import com.skytrace.app.domain.model.ObserverLocation
import com.skytrace.app.services.sky.AstronomyEngine
import com.skytrace.app.ui.theme.*
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SkyMapScreen(
    navController: NavController,
    viewModel: SkyMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Sensor setup for compass
    var azimuth by remember { mutableStateOf(0f) }
    var pitch by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val orientationValues = FloatArray(3)
        val rotationMatrix = FloatArray(9)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                pitch = Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                viewModel.updateSensorData(azimuth, -pitch)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Get location
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateLocation(ObserverLocation(it.latitude, it.longitude, it.altitude))
                    } ?: run {
                        viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
                    }
                }.addOnFailureListener {
                    viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
                }
            } catch (e: Exception) {
                viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
            }
        } else {
            viewModel.updateLocation(ObserverLocation(37.5665, 126.9780, 0.0))
            locationPermission.launchPermissionRequest()
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Black)) {
        // Sky map canvas
        SkyCanvas(
            objects = uiState.filteredObjects,
            phoneAzimuth = uiState.phoneAzimuth,
            phoneAltitude = uiState.phoneAltitude,
            onObjectTap = { obj ->
                viewModel.selectObject(obj)
                showBottomSheet = true
            }
        )

        // Top bar overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(
                "Az: ${azimuth.toInt()}° Alt: ${(-pitch).toInt()}°",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                "${uiState.filteredObjects.size} objects",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        // Compass indicator
        Text(
            text = getCompassDirection(azimuth),
            color = AccentBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        )

        // Filter bar at bottom
        FilterBar(
            uiState = uiState,
            onToggle = { type, enabled -> viewModel.toggleFilter(type, enabled) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Object detail bottom sheet
    if (showBottomSheet && uiState.selectedObject != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = DarkNavy
        ) {
            ObjectDetailSheet(
                obj = uiState.selectedObject!!,
                onPointTelescope = {
                    showBottomSheet = false
                    navController.navigate("telescope/${uiState.selectedObject!!.id}")
                },
                onLogObservation = {
                    showBottomSheet = false
                    val obj = uiState.selectedObject!!
                    navController.navigate("observations/add?objectName=${obj.name}&objectType=${obj.type.name}")
                }
            )
        }
    }
}

@Composable
private fun SkyCanvas(
    objects: List<CelestialObject>,
    phoneAzimuth: Float,
    phoneAltitude: Float,
    onObjectTap: (CelestialObject) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val fovDeg = 60f // field of view

        // Draw objects relative to phone direction
        objects.forEach { obj ->
            val objAz = obj.azimuth ?: return@forEach
            val objAlt = obj.altitude ?: return@forEach

            // Calculate offset from phone direction
            val dAz = ((objAz - phoneAzimuth + 180) % 360 - 180).toFloat()
            val dAlt = (objAlt - phoneAltitude).toFloat()

            // Convert to screen coordinates
            val x = centerX + (dAz / fovDeg) * size.width
            val y = centerY - (dAlt / fovDeg) * size.height

            // Only draw if on screen
            if (x in 0f..size.width && y in 0f..size.height) {
                val radius = when {
                    obj.magnitude == null -> 3f
                    obj.magnitude < 0 -> 8f
                    obj.magnitude < 1 -> 6f
                    obj.magnitude < 2 -> 5f
                    obj.magnitude < 3 -> 4f
                    obj.magnitude < 4 -> 3f
                    else -> 2f
                }

                val color = when (obj.type) {
                    ObjectType.PLANET -> Color(0xFFFFD700)
                    ObjectType.STAR -> Color.White
                    ObjectType.MESSIER, ObjectType.NGC -> Color(0xFF88CCFF)
                    ObjectType.MOON -> Color(0xFFFFF8DC)
                    ObjectType.SUN -> Color(0xFFFFCC00)
                    ObjectType.SATELLITE -> Color(0xFF00FF88)
                    ObjectType.ASTEROID -> Color(0xFFFF8844)
                    else -> Color.Gray
                }

                drawCircle(color = color, radius = radius, center = Offset(x, y))

                // Draw name for brighter objects
                if (obj.magnitude == null || obj.magnitude < 3.0 || obj.type == ObjectType.PLANET) {
                    val result = textMeasurer.measure(
                        AnnotatedString(obj.name),
                        style = TextStyle(color = color.copy(alpha = 0.8f), fontSize = 10.sp)
                    )
                    drawText(result, topLeft = Offset(x + radius + 4, y - 6))
                }
            }
        }

        // Draw crosshair
        drawLine(Color.White.copy(alpha = 0.3f), Offset(centerX - 20, centerY), Offset(centerX + 20, centerY))
        drawLine(Color.White.copy(alpha = 0.3f), Offset(centerX, centerY - 20), Offset(centerX, centerY + 20))
    }
}

@Composable
private fun FilterBar(
    uiState: SkyMapUiState,
    onToggle: (ObjectType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkNavy.copy(alpha = 0.9f))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip("Stars", uiState.showStars) { onToggle(ObjectType.STAR, it) }
        FilterChip("Planets", uiState.showPlanets) { onToggle(ObjectType.PLANET, it) }
        FilterChip("Messier", uiState.showMessier) { onToggle(ObjectType.MESSIER, it) }
        FilterChip("Sats", uiState.showSatellites) { onToggle(ObjectType.SATELLITE, it) }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onToggle: (Boolean) -> Unit) {
    val bg = if (active) AccentBlueDim else Surface
    val textColor = if (active) TextPrimary else TextTertiary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable { onToggle(!active) }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, color = textColor, fontSize = 12.sp)
    }
}

@Composable
private fun ObjectDetailSheet(
    obj: CelestialObject,
    onPointTelescope: () -> Unit,
    onLogObservation: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(obj.name, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        obj.catalogId?.let {
            Text(it, color = AccentBlue, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(obj.type.name, color = TextSecondary, fontSize = 13.sp)
        obj.description?.let {
            Text(it, color = TextTertiary, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))

        // Position data
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("RA")
                DetailValue(AstronomyEngine.formatRA(obj.rightAscension))
            }
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("Dec")
                DetailValue(AstronomyEngine.formatDec(obj.declination))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("Altitude")
                DetailValue("${obj.altitude?.let { "%.1f".format(it) } ?: "—"}°")
            }
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("Azimuth")
                DetailValue("${obj.azimuth?.let { "%.1f".format(it) } ?: "—"}°")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("Magnitude")
                DetailValue(obj.magnitude?.let { "%.1f".format(it) } ?: "—")
            }
            Column(modifier = Modifier.weight(1f)) {
                DetailLabel("Visibility")
                DetailValue(if (obj.isVisible) "Above Horizon" else "Below Horizon")
            }
        }

        obj.constellation?.let {
            Spacer(modifier = Modifier.height(8.dp))
            DetailLabel("Constellation")
            DetailValue(it)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onPointTelescope,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Point Telescope")
            }
            OutlinedButton(
                onClick = onLogObservation,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log")
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(text, color = TextTertiary, fontSize = 11.sp)
}

@Composable
private fun DetailValue(text: String) {
    Text(text, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
}

private fun getCompassDirection(azimuth: Float): String = when {
    azimuth < 22.5f || azimuth >= 337.5f -> "N"
    azimuth < 67.5f -> "NE"
    azimuth < 112.5f -> "E"
    azimuth < 157.5f -> "SE"
    azimuth < 202.5f -> "S"
    azimuth < 247.5f -> "SW"
    azimuth < 292.5f -> "W"
    else -> "NW"
}
