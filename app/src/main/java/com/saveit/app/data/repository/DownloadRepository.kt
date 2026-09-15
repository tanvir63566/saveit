package com.saveit.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.saveit.app.data.local.DownloadDao
import com.saveit.app.data.model.DownloadItem
import com.saveit.app.data.model.DownloadStatus
import com.saveit.app.data.model.MediaInfo
import com.saveit.app.data.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloadDao: DownloadDao,
    private val okHttpClient: OkHttpClient
) {
    private val _downloadProgress = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val downloadProgress = _downloadProgress.asStateFlow()

    fun getAllDownloads(): Flow<List<DownloadItem>> = downloadDao.getAllDownloads()

    suspend fun startDownload(mediaInfo: MediaInfo): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val downloadItem = DownloadItem(
                    sourceUrl = mediaInfo.sourceUrl,
                    title = mediaInfo.title,
                    thumbnailUrl = mediaInfo.thumbnailUrl,
                    status = DownloadStatus.DOWNLOADING,
                    platform = mediaInfo.platform,
                    mediaType = mediaInfo.mediaType
                )

                val id = downloadDao.insertDownload(downloadItem)

                try {
                    if (mediaInfo.mediaType == MediaType.GALLERY) {
                        downloadGallery(id, mediaInfo)
                    } else {
                        downloadSingleFile(id, mediaInfo)
                    }
                    Result.success(id)
                } catch (e: Exception) {
                    downloadDao.updateFailed(id, DownloadStatus.FAILED, e.message ?: "Download failed")
                    Result.failure(e)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun downloadSingleFile(id: Long, mediaInfo: MediaInfo) {
        val fileName = sanitizeFileName("${mediaInfo.title}.${mediaInfo.fileExtension}")
        val url = mediaInfo.downloadUrl

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "SaveIt/1.0")
            .build()

        val response = okHttpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw Exception("Download failed with code: ${response.code}")
        }

        val body = response.body ?: throw Exception("Empty response body")
        val totalBytes = body.contentLength()
        val inputStream = body.byteStream()

        val filePath = saveToMediaStore(fileName, mediaInfo.mediaType, inputStream, totalBytes) { progress ->
            updateProgress(id, progress)
        }

        downloadDao.updateCompleted(
            id = id,
            status = DownloadStatus.COMPLETED,
            filePath = filePath,
            fileSize = totalBytes
        )

        updateProgress(id, 100)
    }

    private suspend fun downloadGallery(id: Long, mediaInfo: MediaInfo) {
        val totalItems = mediaInfo.galleryItems.size
        var completedItems = 0

        for ((index, item) in mediaInfo.galleryItems.withIndex()) {
            val ext = when (item.mediaType) {
                MediaType.VIDEO -> "mp4"
                MediaType.GIF -> "gif"
                else -> "jpg"
            }
            val fileName = sanitizeFileName("${mediaInfo.title}_${index + 1}.$ext")

            val request = Request.Builder()
                .url(item.url)
                .header("User-Agent", "SaveIt/1.0")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body ?: continue
                saveToMediaStore(fileName, item.mediaType, body.byteStream(), body.contentLength()) { _ -> }
                completedItems++
                val overallProgress = (completedItems * 100) / totalItems
                updateProgress(id, overallProgress)
            }
        }

        if (completedItems > 0) {
            downloadDao.updateCompleted(
                id = id,
                status = DownloadStatus.COMPLETED,
                filePath = "Gallery: $completedItems files",
                fileSize = 0
            )
            updateProgress(id, 100)
        } else {
            throw Exception("Failed to download any gallery items")
        }
    }

    private fun saveToMediaStore(
        fileName: String,
        mediaType: MediaType,
        inputStream: java.io.InputStream,
        totalBytes: Long,
        onProgress: (Int) -> Unit
    ): String {
        val (collection, mimeType, relativePath) = when (mediaType) {
            MediaType.VIDEO -> Triple(
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                "video/mp4",
                Environment.DIRECTORY_MOVIES + "/SaveIt"
            )
            MediaType.AUDIO -> Triple(
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                "audio/mpeg",
                Environment.DIRECTORY_MUSIC + "/SaveIt"
            )
            MediaType.GIF -> Triple(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                "image/gif",
                Environment.DIRECTORY_PICTURES + "/SaveIt"
            )
            else -> Triple(
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                "image/jpeg",
                Environment.DIRECTORY_PICTURES + "/SaveIt"
            )
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(collection, contentValues)
            ?: throw Exception("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (totalBytes > 0) {
                    val progress = ((totalBytesRead * 100) / totalBytes).toInt()
                    onProgress(progress.coerceIn(0, 100))
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri.toString()
    }

    private fun updateProgress(id: Long, progress: Int) {
        _downloadProgress.value = _downloadProgress.value.toMutableMap().apply {
            put(id, progress)
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            .take(200)
    }

    suspend fun deleteDownload(item: DownloadItem) {
        downloadDao.deleteDownload(item)
        // Optionally delete the file from storage
        try {
            if (item.filePath.isNotEmpty() && item.filePath.startsWith("content://")) {
                val uri = android.net.Uri.parse(item.filePath)
                context.contentResolver.delete(uri, null, null)
            }
        } catch (_: Exception) { }
    }
}
