package com.skytrace.app.ui.screens.telescope

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skytrace.app.ui.theme.*
import kotlin.math.abs

@Composable
fun TelescopePointingScreen(
    navController: NavController,
    objectId: String,
    viewModel: TelescopePointingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Load object data
    LaunchedEffect(objectId) {
        viewModel.loadObject(objectId)
    }

    // Sensor listening
    var currentAz by remember { mutableStateOf(0f) }
    var currentAlt by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val orientationValues = FloatArray(3)
        val rotationMatrix = FloatArray(9)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationValues)
                currentAz = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                if (currentAz < 0) currentAz += 360f
                currentAlt = -Math.toDegrees(orientationValues[1].toDouble()).toFloat()
                viewModel.updatePhoneDirection(currentAz, currentAlt)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        rotationSensor?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Telescope Pointing", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Target info
        uiState.targetObject?.let { target ->
            Text(target.name, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            target.catalogId?.let {
                Text(it, color = AccentBlue, fontSize = 14.sp)
            }
        } ?: run {
            Text("Loading object...", color = TextSecondary)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Direction guidance
        val targetAz = uiState.targetAzimuth
        val targetAlt = uiState.targetAltitude
        val azDiff = uiState.azimuthDifference
        val altDiff = uiState.altitudeDifference

        if (targetAz != null && targetAlt != null) {
            // Horizontal guidance
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Arrow direction
                    val arrowIcon = when {
                        abs(azDiff) < 2 && abs(altDiff) < 2 -> Icons.Default.CheckCircle
                        abs(azDiff) > abs(altDiff) -> {
                            if (azDiff > 0) Icons.Default.ArrowForward else Icons.Default.ArrowBack
                        }
                        else -> {
                            if (altDiff > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                        }
                    }

                    val isOnTarget = abs(azDiff) < 2 && abs(altDiff) < 2
                    val arrowColor = if (isOnTarget) Success else Warning

                    Icon(
                        arrowIcon,
                        contentDescription = "Direction",
                        tint = arrowColor,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isOnTarget) {
                        Text("ON TARGET", color = Success, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    } else {
                        val instruction = buildString {
                            if (abs(azDiff) > 2) {
                                append(if (azDiff > 0) "Move RIGHT " else "Move LEFT ")
                                append("${abs(azDiff).toInt()}°")
                            }
                            if (abs(altDiff) > 2) {
                                if (isNotEmpty()) append("\n")
                                append(if (altDiff > 0) "Move UP " else "Move DOWN ")
                                append("${abs(altDiff).toInt()}°")
                            }
                        }
                        Text(
                            instruction,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Position details
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET", color = TextTertiary, fontSize = 11.sp)
                    Text("Az: ${targetAz.toInt()}°", color = TextPrimary, fontSize = 15.sp)
                    Text("Alt: ${targetAlt.toInt()}°", color = TextPrimary, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PHONE", color = TextTertiary, fontSize = 11.sp)
                    Text("Az: ${currentAz.toInt()}°", color = TextSecondary, fontSize = 15.sp)
                    Text("Alt: ${currentAlt.toInt()}°", color = TextSecondary, fontSize = 15.sp)
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("DIFF", color = TextTertiary, fontSize = 11.sp)
                    Text("ΔAz: ${azDiff.toInt()}°", color = Warning, fontSize = 15.sp)
                    Text("ΔAlt: ${altDiff.toInt()}°", color = Warning, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Target below horizon warning
            if (targetAlt < 0) {
                Card(colors = CardDefaults.cardColors(containerColor = Surface)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, "Warning", tint = Warning, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Object is below the horizon", color = Warning, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
