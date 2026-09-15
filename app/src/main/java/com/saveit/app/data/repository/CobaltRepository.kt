package com.saveit.app.data.repository

import com.saveit.app.data.model.DownloadQuality
import com.saveit.app.data.model.MediaInfo
import com.saveit.app.data.model.MediaItem
import com.saveit.app.data.model.MediaType
import com.saveit.app.data.model.Platform
import com.saveit.app.data.remote.CobaltApi
import com.saveit.app.data.remote.dto.CobaltRequest
import com.saveit.app.util.UrlParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CobaltRepository @Inject constructor(
    private val cobaltApi: CobaltApi
) {
    suspend fun getMediaInfo(
        url: String,
        platform: Platform,
        quality: String = "720",
        audioOnly: Boolean = false
    ): Result<MediaInfo> {
        return try {
            val request = CobaltRequest(
                url = url,
                videoQuality = quality,
                isAudioOnly = audioOnly,
                audioFormat = "mp3"
            )

            val response = cobaltApi.getDownloadUrl(request)

            if (!response.isSuccessful) {
                return Result.failure(Exception("Cobalt API error: ${response.code()}"))
            }

            val body = response.body()
                ?: return Result.failure(Exception("Empty response from Cobalt API"))

            when (body.status) {
                "redirect", "stream" -> {
                    val downloadUrl = body.url
                        ?: return Result.failure(Exception("No download URL in response"))

                    val mediaType = when {
                        audioOnly -> MediaType.AUDIO
                        else -> MediaType.VIDEO
                    }

                    val qualities = if (platform == Platform.YOUTUBE && !audioOnly) {
                        listOf(
                            DownloadQuality("360p", "360"),
                            DownloadQuality("480p", "480"),
                            DownloadQuality("720p", "720", isDefault = true),
                            DownloadQuality("1080p", "1080"),
                            DownloadQuality("1440p", "1440"),
                            DownloadQuality("4K", "2160")
                        )
                    } else {
                        emptyList()
                    }

                    Result.success(
                        MediaInfo(
                            sourceUrl = url,
                            title = UrlParser.extractTitle(url, platform),
                            thumbnailUrl = null,
                            mediaType = mediaType,
                            platform = platform,
                            downloadUrl = downloadUrl,
                            availableQualities = qualities,
                            fileExtension = if (audioOnly) "mp3" else "mp4"
                        )
                    )
                }

                "picker" -> {
                    val items = body.picker?.map { pickerItem ->
                        MediaItem(
                            url = pickerItem.url,
                            thumbnailUrl = pickerItem.thumb,
                            mediaType = when (pickerItem.type) {
                                "video" -> MediaType.VIDEO
                                else -> MediaType.IMAGE
                            }
                        )
                    } ?: emptyList()

                    if (items.isEmpty()) {
                        return Result.failure(Exception("No media items found"))
                    }

                    Result.success(
                        MediaInfo(
                            sourceUrl = url,
                            title = UrlParser.extractTitle(url, platform),
                            thumbnailUrl = items.firstOrNull()?.thumbnailUrl,
                            mediaType = MediaType.GALLERY,
                            platform = platform,
                            downloadUrl = items.first().url,
                            galleryItems = items
                        )
                    )
                }

                "error" -> {
                    Result.failure(Exception(body.text ?: "Unknown error from Cobalt"))
                }

                else -> {
                    Result.failure(Exception("Unexpected response status: ${body.status}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
