package com.saveit.app.data.repository

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.saveit.app.data.model.MediaInfo
import com.saveit.app.data.model.MediaItem
import com.saveit.app.data.model.MediaType
import com.saveit.app.data.model.Platform
import com.saveit.app.data.remote.RedditApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedditRepository @Inject constructor(
    private val redditApi: RedditApi
) {
    suspend fun getMediaInfo(url: String): Result<MediaInfo> {
        return try {
            val jsonUrl = buildJsonUrl(url)
            val response = redditApi.getPostJson(jsonUrl)

            if (!response.isSuccessful || response.body() == null) {
                return Result.failure(Exception("Failed to fetch Reddit post: ${response.code()}"))
            }

            val jsonArray = response.body()!!
            val postData = jsonArray[0].asJsonObject
                .getAsJsonObject("data")
                .getAsJsonArray("children")[0].asJsonObject
                .getAsJsonObject("data")

            parsePostData(postData, url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildJsonUrl(url: String): String {
        var cleanUrl = url.trim()
        // Remove query params
        cleanUrl = cleanUrl.split("?").first()
        // Remove trailing slash
        cleanUrl = cleanUrl.trimEnd('/')
        // Add .json
        return if (cleanUrl.endsWith(".json")) cleanUrl else "$cleanUrl.json"
    }

    private fun parsePostData(data: JsonObject, sourceUrl: String): Result<MediaInfo> {
        val title = data.get("title")?.asString ?: "Reddit Post"
        val thumbnail = data.get("thumbnail")?.asString
        val postHint = data.get("post_hint")?.asString

        // Check for crosspost
        val crosspostList = data.getAsJsonArray("crosspost_parent_list")
        val effectiveData = if (crosspostList != null && crosspostList.size() > 0) {
            crosspostList[0].asJsonObject
        } else {
            data
        }

        // Check if it's a gallery
        val isGallery = effectiveData.get("is_gallery")?.asBoolean == true
        if (isGallery) {
            return parseGallery(effectiveData, title, thumbnail, sourceUrl)
        }

        // Check for Reddit video (v.redd.it)
        val redditVideo = effectiveData.getAsJsonObject("media")
            ?.getAsJsonObject("reddit_video")
        if (redditVideo != null) {
            return parseRedditVideo(redditVideo, title, thumbnail, sourceUrl)
        }

        // Check for preview video (crossposted videos, gifs)
        val previewVideo = effectiveData.getAsJsonObject("preview")
            ?.getAsJsonObject("reddit_video_preview")
        if (previewVideo != null) {
            return parseRedditVideo(previewVideo, title, thumbnail, sourceUrl)
        }

        // Check for direct image/gif
        val urlStr = effectiveData.get("url_overridden_by_dest")?.asString
            ?: effectiveData.get("url")?.asString

        if (urlStr != null) {
            val mediaType = when {
                urlStr.endsWith(".gif") || urlStr.contains("giphy") -> MediaType.GIF
                urlStr.endsWith(".gifv") -> MediaType.VIDEO
                urlStr.endsWith(".mp4") -> MediaType.VIDEO
                urlStr.endsWith(".jpg") || urlStr.endsWith(".jpeg") ||
                    urlStr.endsWith(".png") || urlStr.endsWith(".webp") ||
                    postHint == "image" -> MediaType.IMAGE
                urlStr.contains("i.redd.it") || urlStr.contains("i.imgur.com") -> MediaType.IMAGE
                else -> MediaType.IMAGE
            }

            val downloadUrl = if (urlStr.endsWith(".gifv")) {
                urlStr.replace(".gifv", ".mp4")
            } else {
                urlStr
            }

            return Result.success(
                MediaInfo(
                    sourceUrl = sourceUrl,
                    title = title,
                    thumbnailUrl = if (thumbnail != "self" && thumbnail != "default" && thumbnail != "nsfw") thumbnail else null,
                    mediaType = mediaType,
                    platform = Platform.REDDIT,
                    downloadUrl = downloadUrl
                )
            )
        }

        return Result.failure(Exception("No downloadable media found in this Reddit post"))
    }

    private fun parseRedditVideo(
        videoData: JsonObject,
        title: String,
        thumbnail: String?,
        sourceUrl: String
    ): Result<MediaInfo> {
        val fallbackUrl = videoData.get("fallback_url")?.asString
            ?: return Result.failure(Exception("No video URL found"))

        // Build audio URL by replacing the quality segment
        val audioUrl = fallbackUrl
            .replace(Regex("DASH_\\d+"), "DASH_AUDIO_128")
            .split("?").first()

        return Result.success(
            MediaInfo(
                sourceUrl = sourceUrl,
                title = title,
                thumbnailUrl = if (thumbnail != "self" && thumbnail != "default" && thumbnail != "nsfw") thumbnail else null,
                mediaType = MediaType.VIDEO,
                platform = Platform.REDDIT,
                downloadUrl = fallbackUrl.split("?").first(),
                audioUrl = audioUrl
            )
        )
    }

    private fun parseGallery(
        data: JsonObject,
        title: String,
        thumbnail: String?,
        sourceUrl: String
    ): Result<MediaInfo> {
        val mediaMetadata = data.getAsJsonObject("media_metadata")
            ?: return Result.failure(Exception("No gallery metadata found"))
        val galleryData = data.getAsJsonObject("gallery_data")
            ?.getAsJsonArray("items")

        val items = mutableListOf<MediaItem>()

        if (galleryData != null) {
            for (item in galleryData) {
                val mediaId = item.asJsonObject.get("media_id")?.asString ?: continue
                val media = mediaMetadata.getAsJsonObject(mediaId) ?: continue
                val mimeType = media.get("m")?.asString ?: "image/jpg"
                val ext = mimeType.substringAfter("/")
                val imageUrl = "https://i.redd.it/$mediaId.$ext"

                items.add(
                    MediaItem(
                        url = imageUrl,
                        mediaType = if (mimeType.startsWith("image/gif")) MediaType.GIF else MediaType.IMAGE
                    )
                )
            }
        } else {
            // Fallback: iterate mediaMetadata keys
            for (key in mediaMetadata.keySet()) {
                val media = mediaMetadata.getAsJsonObject(key)
                val mimeType = media.get("m")?.asString ?: "image/jpg"
                val ext = mimeType.substringAfter("/")
                val imageUrl = "https://i.redd.it/$key.$ext"
                items.add(MediaItem(url = imageUrl, mediaType = MediaType.IMAGE))
            }
        }

        if (items.isEmpty()) {
            return Result.failure(Exception("No images found in gallery"))
        }

        return Result.success(
            MediaInfo(
                sourceUrl = sourceUrl,
                title = title,
                thumbnailUrl = if (thumbnail != "self" && thumbnail != "default" && thumbnail != "nsfw") thumbnail else null,
                mediaType = MediaType.GALLERY,
                platform = Platform.REDDIT,
                downloadUrl = items.first().url,
                galleryItems = items
            )
        )
    }
}
