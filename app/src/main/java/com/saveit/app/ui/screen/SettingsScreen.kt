package com.saveit.app.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var darkTheme by remember { mutableStateOf(false) }
    var defaultQuality by remember { mutableStateOf("720") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Download Settings
            Text(
                text = "Download Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Default quality
                    Text(
                        text = "Default Video Quality",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("360" to "360p", "480" to "480p", "720" to "720p", "1080" to "1080p").forEach { (value, label) ->
                            FilterChip(
                                selected = defaultQuality == value,
                                onClick = { defaultQuality = value },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Save location info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Save Location",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Pictures/SaveIt, Movies/SaveIt, Music/SaveIt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SaveIt",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Version 1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Download media from Reddit, YouTube, and Instagram with ease.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Reddit") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Public,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("YouTube") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.PlayCircle,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("Instagram") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
