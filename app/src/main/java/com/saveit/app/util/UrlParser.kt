package com.saveit.app.util

import com.saveit.app.data.model.Platform

object UrlParser {

    fun detectPlatform(url: String): Platform {
        val lowerUrl = url.lowercase().trim()
        return when {
            lowerUrl.contains("reddit.com") || lowerUrl.contains("redd.it") -> Platform.REDDIT
            lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be") ||
                lowerUrl.contains("youtube.shorts") -> Platform.YOUTUBE
            lowerUrl.contains("instagram.com") || lowerUrl.contains("instagr.am") -> Platform.INSTAGRAM
            else -> Platform.UNKNOWN
        }
    }

    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        return try {
            val pattern = Regex("^https?://(www\\.)?(reddit\\.com|redd\\.it|youtube\\.com|youtu\\.be|instagram\\.com|instagr\\.am)/.+")
            pattern.matches(trimmed)
        } catch (_: Exception) {
            false
        }
    }

    fun extractTitle(url: String, platform: Platform): String {
        return when (platform) {
            Platform.YOUTUBE -> {
                val videoId = extractYouTubeId(url)
                "YouTube Video${if (videoId != null) \" ($videoId)\" else \"\"}"
            }
            Platform.INSTAGRAM -> {
                when {
                    url.contains("/reel/") || url.contains("/reels/") -> "Instagram Reel"
                    url.contains("/p/") -> "Instagram Post"
                    url.contains("/stories/") -> "Instagram Story"
                    else -> "Instagram Media"
                }
            }
            Platform.REDDIT -> "Reddit Post"
            Platform.UNKNOWN -> "Media"
        }
    }

    private fun extractYouTubeId(url: String): String? {
        val patterns = listOf(
            Regex("(?:v=|/)([a-zA-Z0-9_-]{11})"),
            Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
            Regex("shorts/([a-zA-Z0-9_-]{11})")
        )
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) return match.groupValues[1]
        }
        return null
    }

    fun getPlatformDisplayName(platform: Platform): String {
        return when (platform) {
            Platform.REDDIT -> "Reddit"
            Platform.YOUTUBE -> "YouTube"
            Platform.INSTAGRAM -> "Instagram"
            Platform.UNKNOWN -> "Unknown"
        }
    }
}
