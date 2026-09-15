package com.saveit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saveit.app.data.model.MediaType
import com.saveit.app.data.model.Platform
import com.saveit.app.ui.theme.*

@Composable
fun PlatformBadge(
    platform: Platform,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, text) = when (platform) {
        Platform.REDDIT -> RedditOrange to "Reddit"
        Platform.YOUTUBE -> YouTubeRed to "YouTube"
        Platform.INSTAGRAM -> InstagramPink to "Instagram"
        Platform.UNKNOWN -> Color.Gray to "Unknown"
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun MediaTypeBadge(
    mediaType: MediaType,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (mediaType) {
        MediaType.IMAGE -> Icons.Default.Photo to "Image"
        MediaType.VIDEO -> Icons.Default.PlayCircle to "Video"
        MediaType.GIF -> Icons.Default.Image to "GIF"
        MediaType.AUDIO -> Icons.Default.MusicNote to "Audio"
        MediaType.GALLERY -> Icons.Default.PhotoLibrary to "Gallery"
        MediaType.UNKNOWN -> Icons.Default.OndemandVideo to "Media"
    }

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = " $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
