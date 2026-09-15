package com.saveit.app.data.model

enum class Platform {
    REDDIT, YOUTUBE, INSTAGRAM, UNKNOWN
}

enum class MediaType {
    IMAGE, VIDEO, GIF, AUDIO, GALLERY, UNKNOWN
}

data class DownloadQuality(
    val label: String,
    val value: String,
    val isDefault: Boolean = false
)

data class MediaItem(
    val url: String,
    val thumbnailUrl: String? = null,
    val mediaType: MediaType = MediaType.UNKNOWN,
    val quality: String? = null
)

data class MediaInfo(
    val sourceUrl: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val mediaType: MediaType,
    val platform: Platform,
    val downloadUrl: String,
    val audioUrl: String? = null,
    val availableQualities: List<DownloadQuality> = emptyList(),
    val galleryItems: List<MediaItem> = emptyList(),
    val fileExtension: String = when (mediaType) {
        MediaType.IMAGE -> "jpg"
        MediaType.VIDEO -> "mp4"
        MediaType.GIF -> "gif"
        MediaType.AUDIO -> "mp3"
        MediaType.GALLERY -> "jpg"
        MediaType.UNKNOWN -> "bin"
    }
)
