package com.saveit.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.saveit.app.data.model.DownloadItem
import com.saveit.app.data.model.DownloadStatus
import com.saveit.app.data.model.Platform
import com.saveit.app.ui.components.PlatformBadge
import com.saveit.app.ui.theme.*
import com.saveit.app.ui.viewmodel.DownloadsViewModel
import com.saveit.app.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Downloads",
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
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == null,
                    onClick = { viewModel.onFilterChanged(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = uiState.selectedFilter == Platform.REDDIT,
                    onClick = { viewModel.onFilterChanged(Platform.REDDIT) },
                    label = { Text("Reddit") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RedditOrange.copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = uiState.selectedFilter == Platform.YOUTUBE,
                    onClick = { viewModel.onFilterChanged(Platform.YOUTUBE) },
                    label = { Text("YouTube") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = YouTubeRed.copy(alpha = 0.2f)
                    )
                )
                FilterChip(
                    selected = uiState.selectedFilter == Platform.INSTAGRAM,
                    onClick = { viewModel.onFilterChanged(Platform.INSTAGRAM) },
                    label = { Text("Instagram") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = InstagramPink.copy(alpha = 0.2f)
                    )
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.downloads.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "\uD83D\uDCE5",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No downloads yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Downloaded files will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.downloads,
                        key = { it.id }
                    ) { download ->
                        DownloadItemCard(
                            item = download,
                            onDelete = { viewModel.deleteDownload(download) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    item: DownloadItem,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            if (item.thumbnailUrl != null) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .padding(0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.mediaType) {
                            com.saveit.app.data.model.MediaType.VIDEO -> Icons.Default.PlayCircle
                            com.saveit.app.data.model.MediaType.AUDIO -> Icons.Default.MusicNote
                            com.saveit.app.data.model.MediaType.IMAGE -> Icons.Default.Photo
                            com.saveit.app.data.model.MediaType.GIF -> Icons.Default.Image
                            else -> Icons.Default.Download
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlatformBadge(platform = item.platform)
                    Text(
                        text = FileUtils.formatTimestamp(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Status
                when (item.status) {
                    DownloadStatus.DOWNLOADING -> {
                        LinearProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        )
                    }
                    DownloadStatus.FAILED -> {
                        Text(
                            text = "Failed: ${item.errorMessage ?: "Unknown error"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed
                        )
                    }
                    DownloadStatus.COMPLETED -> {
                        if (item.fileSize > 0) {
                            Text(
                                text = FileUtils.formatFileSize(item.fileSize),
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen
                            )
                        }
                    }
                    else -> {}
                }
            }

            // More options
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null) }
                    )
                }
            }
        }
    }
}
