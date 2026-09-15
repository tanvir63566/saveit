package com.saveit.app.ui.screen

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saveit.app.data.model.Platform
import com.saveit.app.ui.components.DownloadButton
import com.saveit.app.ui.components.DownloadButtonState
import com.saveit.app.ui.components.MediaPreviewCard
import com.saveit.app.ui.theme.*
import com.saveit.app.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    onSharedUrlConsumed: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Handle shared URL
    LaunchedEffect(sharedUrl) {
        if (sharedUrl != null) {
            viewModel.onUrlChanged(sharedUrl)
            onSharedUrlConsumed()
            viewModel.fetchMediaInfo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SaveIt",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // URL Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.url,
                        onValueChange = { viewModel.onUrlChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Paste URL here...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = when (uiState.platform) {
                                    Platform.REDDIT -> RedditOrange
                                    Platform.YOUTUBE -> YouTubeRed
                                    Platform.INSTAGRAM -> InstagramPink
                                    Platform.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        trailingIcon = {
                            Row {
                                if (uiState.url.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearState() }) {
                                        Icon(Icons.Default.Clear, "Clear")
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                        if (text != null) {
                                            viewModel.onUrlChanged(text)
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ContentPaste,
                                        "Paste",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Platform indicator
                    AnimatedVisibility(visible = uiState.platform != Platform.UNKNOWN) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (uiState.platform) {
                                    Platform.REDDIT -> "\uD83E\uDD16 Reddit detected"
                                    Platform.YOUTUBE -> "\u25B6\uFE0F YouTube detected"
                                    Platform.INSTAGRAM -> "\uD83D\uDCF7 Instagram detected"
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // YouTube options
                    AnimatedVisibility(visible = uiState.platform == Platform.YOUTUBE) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            // Audio only toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Audio Only (MP3)",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = uiState.audioOnly,
                                    onCheckedChange = { viewModel.onAudioOnlyChanged(it) }
                                )
                            }

                            // Quality selector
                            AnimatedVisibility(visible = !uiState.audioOnly) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        "Quality",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf("360" to "360p", "720" to "720p", "1080" to "1080p").forEach { (value, label) ->
                                            FilterChip(
                                                selected = uiState.selectedQuality == value,
                                                onClick = { viewModel.onQualityChanged(value) },
                                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fetch / Download button
                    if (uiState.mediaInfo == null) {
                        Button(
                            onClick = { viewModel.fetchMediaInfo() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = uiState.isValidUrl && !uiState.isLoading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetching...")
                            } else {
                                Text("Fetch Media Info")
                            }
                        }
                    }
                }
            }

            // Error message
            AnimatedVisibility(visible = uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorRed.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Media Preview
            AnimatedVisibility(
                visible = uiState.mediaInfo != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                uiState.mediaInfo?.let { mediaInfo ->
                    Column(
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        MediaPreviewCard(mediaInfo = mediaInfo)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Download button
                        val buttonState = when {
                            uiState.downloadComplete -> DownloadButtonState.COMPLETED
                            uiState.isDownloading -> DownloadButtonState.DOWNLOADING
                            uiState.error != null && uiState.mediaInfo != null -> DownloadButtonState.ERROR
                            else -> DownloadButtonState.IDLE
                        }

                        DownloadButton(
                            state = buttonState,
                            progress = uiState.downloadProgress,
                            onClick = {
                                if (buttonState == DownloadButtonState.COMPLETED) {
                                    viewModel.clearState()
                                } else {
                                    viewModel.startDownload()
                                }
                            }
                        )
                    }
                }
            }

            // Empty state hint
            AnimatedVisibility(
                visible = uiState.url.isEmpty() && uiState.mediaInfo == null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\uD83D\uDD17",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Paste a URL to get started",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Supports Reddit • YouTube • Instagram",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
