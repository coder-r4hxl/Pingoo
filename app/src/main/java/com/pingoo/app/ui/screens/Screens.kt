package com.pingoo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pingoo.app.domain.model.Destination
import com.pingoo.app.domain.model.Language
import com.pingoo.app.domain.usecase.ValidationResult
import com.pingoo.app.ui.i18n.AppStrings

@Composable
fun HomeScreen(
    strings: AppStrings,
    activeDestinations: List<Destination>,
    onNavigateToDestinations: () -> Unit,
    onToggleDestination: (Destination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.homeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = strings.activeAlarms, style = MaterialTheme.typography.titleMedium)
            Button(onClick = onNavigateToDestinations) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(strings.quickAddDestination)
            }
        }

        if (activeDestinations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = strings.noActiveAlarms, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(activeDestinations, key = { it.id }) { destination ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = destination.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Lat: ${destination.latitude}, Lng: ${destination.longitude}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${strings.arrivalRadius}: ${destination.arrivalRadiusMeters}m",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = destination.isActive,
                                onCheckedChange = { onToggleDestination(destination) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DestinationsScreen(
    strings: AppStrings,
    destinations: List<Destination>,
    onAddDestination: (String, String, String, String, (ValidationResult) -> Unit) -> Unit,
    onDeleteDestination: (Destination) -> Unit,
    onToggleDestination: (Destination) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf("") }
    var lng by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf("500") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = strings.addDestination)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(text = strings.destinationsTitle, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (destinations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = strings.noDestinationsYet)
                }
            } else {
                LazyColumn {
                    items(destinations, key = { it.id }) { dest ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = dest.name, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Coordinates: ${dest.latitude}, ${dest.longitude}")
                                    Text(text = "${strings.arrivalRadius}: ${dest.arrivalRadiusMeters}m")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = dest.isActive,
                                        onCheckedChange = { onToggleDestination(dest) }
                                    )
                                    IconButton(onClick = { onDeleteDestination(dest) }) {
                                        Icon(Icons.Default.Delete, contentDescription = strings.delete)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    errorMessage = null
                },
                title = { Text(strings.addDestination) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(strings.destinationName) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lat,
                            onValueChange = { lat = it },
                            label = { Text(strings.latitude) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lng,
                            onValueChange = { lng = it },
                            label = { Text(strings.longitude) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it },
                            label = { Text(strings.arrivalRadius) },
                            singleLine = true
                        )
                        errorMessage?.let { error ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = error, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onAddDestination(name, lat, lng, radius) { result ->
                                when (result) {
                                    is ValidationResult.Success -> {
                                        showDialog = false
                                        name = ""
                                        lat = ""
                                        lng = ""
                                        errorMessage = null
                                    }
                                    is ValidationResult.Error -> {
                                        errorMessage = result.message
                                    }
                                }
                            }
                        }
                    ) {
                        Text(strings.save)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
fun JourneyScreen(strings: AppStrings) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = strings.journeyTitle, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = strings.liveGpsStatus, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Placeholder action for Phase 2 */ }) {
            Text(strings.startMonitoring)
        }
    }
}

@Composable
fun HistoryScreen(strings: AppStrings) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = strings.historyTitle, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = strings.noHistoryYet)
        }
    }
}

@Composable
fun SettingsScreen(
    strings: AppStrings,
    selectedLanguage: Language,
    isVibrationEnabled: Boolean,
    onLanguageSelected: (Language) -> Unit,
    onVibrationToggled: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = strings.settingsTitle, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = strings.languageSelection, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = selectedLanguage == Language.ENGLISH,
                        onClick = { onLanguageSelected(Language.ENGLISH) },
                        label = { Text("English") }
                    )
                    FilterChip(
                        selected = selectedLanguage == Language.HINDI,
                        onClick = { onLanguageSelected(Language.HINDI) },
                        label = { Text("हिन्दी (Hindi)") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = strings.vibrationSetting, style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggled
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = strings.appVersion, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = strings.phaseNotice, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
