package com.saveit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.saveit.app.data.model.MediaInfo

@Composable
fun MediaPreviewCard(
    mediaInfo: MediaInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail
            if (mediaInfo.thumbnailUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    AsyncImage(
                        model = mediaInfo.thumbnailUrl,
                        contentDescription = mediaInfo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Badges overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlatformBadge(platform = mediaInfo.platform)
                        MediaTypeBadge(mediaType = mediaInfo.mediaType)
                    }
                }
            } else {
                // No thumbnail - show badges in a colored header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlatformBadge(platform = mediaInfo.platform)
                        MediaTypeBadge(mediaType = mediaInfo.mediaType)
                    }
                }
            }

            // Title and info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = mediaInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (mediaInfo.thumbnailUrl == null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlatformBadge(platform = mediaInfo.platform)
                        MediaTypeBadge(mediaType = mediaInfo.mediaType)
                    }
                }

                if (mediaInfo.galleryItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${mediaInfo.galleryItems.size} items in gallery",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
