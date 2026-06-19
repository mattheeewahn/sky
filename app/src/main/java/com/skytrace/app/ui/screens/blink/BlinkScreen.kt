package com.skytrace.app.ui.screens.blink

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.skytrace.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BlinkScreen(
    navController: NavController,
    viewModel: BlinkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                Text("Image Blink", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
            IconButton(onClick = { viewModel.addImage() }) {
                Icon(Icons.Default.Add, "Add Image", tint = AccentBlue)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.images.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Compare, "Blink", tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No images loaded", color = TextSecondary)
                    Text("Add 2+ images to start blinking", color = TextTertiary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.addImage() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Images")
                    }
                }
            }
        } else {
            // Blink viewer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DarkNavy)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            viewModel.markPosition(offset.x, offset.y)
                        }
                    }
            ) {
                // Display current frame
                if (uiState.images.isNotEmpty() && uiState.currentFrame < uiState.images.size) {
                    val currentImage = uiState.images[uiState.currentFrame]
                    Image(
                        painter = rememberAsyncImagePainter(currentImage),
                        contentDescription = "Frame ${uiState.currentFrame + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                // Frame indicator
                Text(
                    "Frame ${uiState.currentFrame + 1} / ${uiState.images.size}",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Marked position indicator
                uiState.markedX?.let { x ->
                    uiState.markedY?.let { y ->
                        // Draw crosshair at marked position
                        Text(
                            "+",
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.offset(x = x.dp - 8.dp, y = y.dp - 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Blink speed control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Blink Speed", color = TextSecondary, fontSize = 13.sp)
                        Text("${uiState.blinkSpeedMs}ms", color = TextTertiary, fontSize = 12.sp)
                    }
                    Slider(
                        value = uiState.blinkSpeedMs.toFloat(),
                        onValueChange = { viewModel.setBlinkSpeed(it.toInt()) },
                        valueRange = 100f..2000f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue
                        )
                    )

                    // Brightness/contrast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Brightness", color = TextSecondary, fontSize = 13.sp)
                        Text("${uiState.brightness}", color = TextTertiary, fontSize = 12.sp)
                    }
                    Slider(
                        value = uiState.brightness,
                        onValueChange = { viewModel.setBrightness(it) },
                        valueRange = -1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Play/Pause and navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousFrame() }) {
                            Icon(Icons.Default.SkipPrevious, "Previous", tint = TextPrimary)
                        }
                        IconButton(onClick = { viewModel.togglePlay() }) {
                            Icon(
                                if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                "Play/Pause",
                                tint = AccentBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.nextFrame() }) {
                            Icon(Icons.Default.SkipNext, "Next", tint = TextPrimary)
                        }
                    }

                    // Marked position info
                    uiState.markedX?.let { x ->
                        uiState.markedY?.let { y ->
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Marked Position: (${x.toInt()}, ${y.toInt()}) px", color = AccentBlue, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Blink animation
    LaunchedEffect(uiState.isPlaying, uiState.blinkSpeedMs) {
        while (uiState.isPlaying && uiState.images.size >= 2) {
            delay(uiState.blinkSpeedMs.toLong())
            viewModel.nextFrame()
        }
    }
}
