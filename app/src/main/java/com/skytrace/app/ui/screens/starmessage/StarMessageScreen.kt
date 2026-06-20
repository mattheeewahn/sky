package com.skytrace.app.ui.screens.starmessage

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skytrace.app.domain.model.MessageTier
import com.skytrace.app.domain.model.StarMessage
import com.skytrace.app.ui.theme.*

@Composable
fun StarMessageScreen(
    navController: NavController,
    viewModel: StarMessageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        when (uiState.step) {
            MessageStep.SELECT_TARGET -> TargetStep(uiState, viewModel, navController)
            MessageStep.COMPOSE -> ComposeStep(uiState, viewModel)
            MessageStep.CONFIRM -> ConfirmStep(uiState, viewModel)
            MessageStep.TRANSMITTING -> TransmittingStep(uiState)
            MessageStep.CERTIFICATE -> CertificateStep(uiState, viewModel)
        }
    }
}

@Composable
private fun TargetStep(
    uiState: StarMessageUiState,
    viewModel: StarMessageViewModel,
    navController: NavController
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Send to Space", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Choose a destination", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        // Search
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.searchTargets(it) },
            placeholder = { Text("Search stars, galaxies...", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(uiState.searchResults) { obj ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkNavy),
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectTarget(obj) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(obj.name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                "${obj.type.name}${obj.catalogId?.let { " • $it" } ?: ""}",
                                color = TextTertiary, fontSize = 12.sp
                            )
                        }
                        Icon(Icons.Default.Send, "Select", tint = AccentBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // History section
            if (uiState.sentMessages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sent Messages", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(uiState.sentMessages.take(5)) { msg ->
                    SentMessageRow(msg)
                }
            }
        }
    }
}

@Composable
private fun SentMessageRow(msg: StarMessage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(msg.status.emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(msg.targetName, color = TextPrimary, fontSize = 13.sp)
                Text(
                    msg.message.take(30) + if (msg.message.length > 30) "..." else "",
                    color = TextTertiary, fontSize = 11.sp
                )
            }
            msg.certificateId?.let {
                Text(it, color = AccentBlue, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ComposeStep(uiState: StarMessageUiState, viewModel: StarMessageViewModel) {
    val target = uiState.selectedTarget ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("To: ${target.name}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sender name
        OutlinedTextField(
            value = uiState.senderName,
            onValueChange = { viewModel.updateSenderName(it) },
            label = { Text("Your name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Message
        OutlinedTextField(
            value = uiState.message,
            onValueChange = { viewModel.updateMessage(it) },
            label = { Text("Message") },
            placeholder = { Text("What do you want to say to the universe?", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue, unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
            )
        )

        Text(
            "${uiState.message.length} / ${uiState.selectedTier.maxChars}",
            color = TextTertiary, fontSize = 11.sp,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Send button
        Button(
            onClick = { viewModel.goToConfirm() },
            enabled = uiState.message.isNotBlank() && uiState.senderName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Continue", fontSize = 16.sp)
        }
    }
}

@Composable
private fun ConfirmStep(uiState: StarMessageUiState, viewModel: StarMessageViewModel) {
    val target = uiState.selectedTarget ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Confirm Transmission", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Destination", color = TextTertiary, fontSize = 12.sp)
                Text(target.name, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                target.catalogId?.let { Text(it, color = AccentBlue, fontSize = 13.sp) }

                Spacer(modifier = Modifier.height(16.dp))

                Text("From", color = TextTertiary, fontSize = 12.sp)
                Text(uiState.senderName, color = TextPrimary, fontSize = 15.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Message", color = TextTertiary, fontSize = 12.sp)
                Text(uiState.message, color = TextSecondary, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Text("Frequency", color = TextTertiary, fontSize = 12.sp)
                Text("1420.405 MHz (Hydrogen Line)", color = TextPrimary, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.transmit() },
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text("Transmit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TransmittingStep(uiState: StarMessageUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📡", fontSize = 64.sp, modifier = Modifier.alpha(alpha))

        Spacer(modifier = Modifier.height(24.dp))

        val phase = when {
            uiState.transmissionProgress < 0.2f -> "Encoding message..."
            uiState.transmissionProgress < 0.8f -> "Transmitting..."
            else -> "Confirming..."
        }
        Text(phase, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = { uiState.transmissionProgress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = AccentBlue,
            trackColor = Surface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "${(uiState.transmissionProgress * 100).toInt()}%",
            color = TextSecondary, fontSize = 14.sp
        )
    }
}

@Composable
private fun CertificateStep(uiState: StarMessageUiState, viewModel: StarMessageViewModel) {
    val msg = uiState.currentTransmission ?: return

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("✅", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Transmitted", color = Success, fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // Certificate card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("TRANSMISSION CERTIFICATE", color = TextTertiary, fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(msg.targetName, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                msg.targetCatalogId?.let { Text(it, color = AccentBlue, fontSize = 13.sp) }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("\"${msg.message}\"", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("From", color = TextTertiary, fontSize = 11.sp)
                        Text(msg.senderName, color = TextPrimary, fontSize = 13.sp)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text("Frequency", color = TextTertiary, fontSize = 11.sp)
                        Text("${msg.frequency} MHz", color = TextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                msg.estimatedArrivalYears?.let { years ->
                    val arrivalText = when {
                        years < 0.001 -> "${(years * 365.25 * 24 * 60).toInt()} minutes"
                        years < 1 -> "${(years * 365.25).toInt()} days"
                        years < 1000 -> "${years.toInt()} years"
                        years < 1000000 -> "${(years / 1000).toInt()}k years"
                        else -> "${(years / 1000000).toInt()}M years"
                    }
                    Text("Estimated arrival: $arrivalText", color = Warning, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                msg.certificateId?.let {
                    Text(it, color = AccentBlue, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.reset() },
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Send Another", fontSize = 15.sp)
        }
    }
}
