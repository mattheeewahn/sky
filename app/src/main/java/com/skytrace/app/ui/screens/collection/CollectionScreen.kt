package com.skytrace.app.ui.screens.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skytrace.app.domain.model.CollectionEntry
import com.skytrace.app.domain.model.CollectionSummary
import com.skytrace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CollectionScreen(
    navController: NavController,
    viewModel: CollectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                }
                Text("Collection", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
            }
        }

        // Summary cards
        item {
            uiState.summary?.let { summary ->
                CollectionSummaryCard(summary)
            }
        }

        // Category progress
        item {
            uiState.summary?.let { summary ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryProgress("Planets", summary.observedPlanets, summary.totalPlanets)
                    CategoryProgress("Messier Objects", summary.observedMessier, summary.totalMessier)
                    CategoryProgress("NGC Objects", summary.observedNGC, 100) // Show progress toward 100
                    CategoryProgress("Asteroids", summary.observedAsteroids, 10)
                    CategoryProgress("Comets", summary.observedComets, 5)
                    CategoryProgress("Satellites", summary.observedSatellites, 20)
                }
            }
        }

        // Recent observations
        if (uiState.entries.isNotEmpty()) {
            item {
                Text(
                    "Recent Observations",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(uiState.entries.take(20)) { entry ->
                CollectionEntryRow(entry)
            }
        }
    }
}

@Composable
private fun CollectionSummaryCard(summary: CollectionSummary) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Total Observed", color = TextSecondary, fontSize = 13.sp)
            Text(
                "${summary.overallObserved}",
                color = TextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Text("unique objects", color = TextTertiary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CategoryProgress(label: String, observed: Int, total: Int) {
    val progress = if (total > 0) observed.toFloat() / total else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, color = TextPrimary, fontSize = 14.sp)
                Text("$observed / $total", color = TextSecondary, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentBlue,
                trackColor = TextTertiary.copy(alpha = 0.2f)
            )
            if (progress >= 1.0f) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("✓ Complete", color = Success, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CollectionEntryRow(entry: CollectionEntry) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.objectName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row {
                    Text(entry.objectType.name, color = TextTertiary, fontSize = 12.sp)
                    entry.catalogId?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, color = AccentBlue, fontSize = 12.sp)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${entry.observationCount}x", color = TextSecondary, fontSize = 13.sp)
                Text(
                    dateFormat.format(Date(entry.lastObserved)),
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
