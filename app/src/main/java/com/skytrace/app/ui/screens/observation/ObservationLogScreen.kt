package com.skytrace.app.ui.screens.observation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.skytrace.app.domain.model.Observation
import com.skytrace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ObservationLogScreen(
    navController: NavController,
    viewModel: ObservationLogViewModel = hiltViewModel()
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
                Text("Observation Log", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
            Row {
                IconButton(onClick = { viewModel.exportCsv() }) {
                    Icon(Icons.Default.Download, "Export CSV", tint = TextSecondary)
                }
                FloatingActionButton(
                    onClick = { navController.navigate("observations/add?objectName=&objectType=") },
                    containerColor = AccentBlue,
                    contentColor = TextPrimary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Search
        var searchQuery by remember { mutableStateOf("") }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.search(it)
            },
            placeholder = { Text("Search observations...", color = TextTertiary) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = TextTertiary,
                cursorColor = AccentBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Count
        Text(
            "${uiState.observations.size} observations",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.observations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EditNote, "No observations", tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No observations yet", color = TextSecondary)
                    Text("Tap + to add your first observation", color = TextTertiary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.observations) { observation ->
                    ObservationRow(observation) {
                        // Navigate to edit (reuse add screen)
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationRow(observation: Observation, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(observation.objectName, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    dateFormat.format(Date(observation.dateTime)),
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                observation.catalogId?.let {
                    Text(it, color = AccentBlue, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(observation.objectType.name, color = TextTertiary, fontSize = 12.sp)
                observation.telescope?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, color = TextTertiary, fontSize = 12.sp)
                }
            }
            observation.notes?.let {
                if (it.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(it, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
                }
            }
        }
    }
}
