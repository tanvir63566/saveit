package com.saveit.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceUrl: String,
    val title: String,
    val filePath: String = "",
    val thumbnailUrl: String? = null,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,
    val fileSize: Long = 0,
    val downloadedSize: Long = 0,
    val platform: Platform = Platform.UNKNOWN,
    val mediaType: MediaType = MediaType.UNKNOWN,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
