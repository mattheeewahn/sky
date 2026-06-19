package com.skytrace.app.ui.screens.search

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
import com.skytrace.app.domain.model.CelestialObject
import com.skytrace.app.domain.model.ObjectType
import com.skytrace.app.services.sky.AstronomyEngine
import com.skytrace.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
            }
            Text("Object Search", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.search(it) },
            placeholder = { Text("Search stars, planets, Messier, asteroids...", color = TextTertiary) },
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

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ObjectType.entries.filter {
                it in listOf(ObjectType.STAR, ObjectType.PLANET, ObjectType.MESSIER, ObjectType.NGC, ObjectType.ASTEROID, ObjectType.SATELLITE)
            }.forEach { type ->
                val active = uiState.typeFilter == type
                FilterChip(
                    selected = active,
                    onClick = { viewModel.toggleTypeFilter(if (active) null else type) },
                    label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlueDim,
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results
        if (uiState.isSearching) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else if (uiState.results.isEmpty() && uiState.query.isNotBlank()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("No results found", color = TextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.results) { obj ->
                    SearchResultRow(obj) {
                        navController.navigate("telescope/${obj.id}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(obj: CelestialObject, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type icon
            val icon = when (obj.type) {
                ObjectType.PLANET -> Icons.Default.Public
                ObjectType.STAR -> Icons.Default.Star
                ObjectType.MESSIER, ObjectType.NGC -> Icons.Default.Lens
                ObjectType.SATELLITE -> Icons.Default.Satellite
                ObjectType.ASTEROID -> Icons.Default.Radar
                ObjectType.MOON -> Icons.Default.NightsStay
                else -> Icons.Default.Circle
            }
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(obj.name, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row {
                    obj.catalogId?.let {
                        Text(it, color = AccentBlue, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(obj.type.name, color = TextTertiary, fontSize = 12.sp)
                    obj.constellation?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(it, color = TextTertiary, fontSize = 12.sp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    AstronomyEngine.formatRA(obj.rightAscension),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    AstronomyEngine.formatDec(obj.declination),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                obj.magnitude?.let {
                    Text("Mag ${String.format("%.1f", it)}", color = TextTertiary, fontSize = 11.sp)
                }
            }
        }
    }
}
