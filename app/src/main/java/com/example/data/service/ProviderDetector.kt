package com.example.data.service

object ProviderDetector {
    fun detectProvider(url: String): String {
        val lower = url.trim().lowercase()
        return when {
            lower.contains("youtube.com") || lower.contains("youtu.be") -> "YouTube"
            lower.contains("spotify.com") -> "Spotify"
            lower.contains("instagram.com") || lower.contains("instagr.am") -> "Instagram"
            lower.contains("facebook.com") || lower.contains("fb.watch") || lower.contains("fb.com") -> "Facebook"
            lower.contains("tiktok.com") -> "TikTok"
            lower.contains("pinterest.com") || lower.contains("pin.it") -> "Pinterest"
            else -> "Direct / Web"
        }
    }

    fun isInstagramPhotoPost(url: String): Boolean {
        val lower = url.trim().lowercase()
        return lower.contains("instagram.com/p/") || lower.contains("instagr.am/p/")
    }

    fun extractYouTubeVideoId(url: String): String? {
        val trimmed = url.trim()
        val patterns = listOf(
            Regex("""(?:youtube\.com/(?:watch\?v=|embed/|v/|shorts/)|youtu\.be/)([a-zA-Z0-9_-]{11})"""),
            Regex("""^[a-zA-Z0-9_-]{11}$""")
        )
        for (p in patterns) {
            val match = p.find(trimmed)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun extractInstagramCode(url: String): String? {
        val trimmed = url.trim()
        val regex = Regex("""instagram\.com/(?:reel|p|tv|stories)/([a-zA-Z0-9_-]+)""")
        return regex.find(trimmed)?.groupValues?.getOrNull(1)
    }

    fun extractFacebookVideoId(url: String): String? {
        val trimmed = url.trim()
        val regex = Regex("""(?:facebook\.com/(?:watch\?v=|reel/|videos/)|fb\.watch/)([a-zA-Z0-9_-]+)""")
        return regex.find(trimmed)?.groupValues?.getOrNull(1)
    }
}
