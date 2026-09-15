package com.saveit.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saveit.app.ui.theme.ErrorRed
import com.saveit.app.ui.theme.SuccessGreen

enum class DownloadButtonState {
    IDLE, LOADING, DOWNLOADING, COMPLETED, ERROR
}

@Composable
fun DownloadButton(
    state: DownloadButtonState,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "download_progress"
    )

    when (state) {
        DownloadButtonState.IDLE -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download", style = MaterialTheme.typography.titleMedium)
            }
        }

        DownloadButtonState.LOADING -> {
            OutlinedButton(
                onClick = { },
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = false
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fetching info...", style = MaterialTheme.typography.titleMedium)
            }
        }

        DownloadButtonState.DOWNLOADING -> {
            OutlinedButton(
                onClick = { },
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = false
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Downloading ${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        DownloadButtonState.COMPLETED -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Downloaded ✓", style = MaterialTheme.typography.titleMedium)
            }
        }

        DownloadButtonState.ERROR -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry Download", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
