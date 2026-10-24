package com.example.data.model

enum class MediaFormat(
    val extension: String,
    val mimeType: String,
    val isVideo: Boolean,
    val labelText: String
) {
    MP4("mp4", "video/mp4", true, "MP4 Video"),
    WEBM("webm", "video/webm", true, "WebM Video"),
    MP3("mp3", "audio/mpeg", false, "MP3 Audio"),
    M4A("m4a", "audio/mp4", false, "M4A Studio AAC"),
    FLAC("flac", "audio/flac", false, "FLAC Hi-Res"),
    WAV("wav", "audio/wav", false, "WAV Studio PCM")
}

data class QualityOption(
    val id: String,
    val format: MediaFormat,
    val label: String,
    val subLabel: String? = null,
    val badge: String? = null,
    val resolutionOrBitrate: String,
    val estimatedSizeBytes: Long = -1L,
    val downloadUrl: String,
    val companionAudioUrl: String? = null,
    val codec: String = "Auto",
    val containerExtension: String = format.extension,
    val includesAudio: Boolean = true,
    val requiresAudioExtractionFromVideo: Boolean = false
)

data class MediaAnalysisResult(
    val mediaId: String,
    val originalUrl: String,
    val normalizedUrl: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String?,
    val durationSeconds: Int,
    val durationFormatted: String,
    val providerName: String,
    val isAuthorizedStream: Boolean = true,
    val authorizationLimitationNotice: String? = null,
    val externalLaunchUrl: String? = null,
    val videoOptions: List<QualityOption> = emptyList(),
    val audioOptions: List<QualityOption> = emptyList(),
    val timestampAnalyzed: Long = System.currentTimeMillis()
)

enum class DownloadJobState(val displayName: String) {
    QUEUED("In Queue"),
    PREPARING("Preparing"),
    DOWNLOADING("Downloading"),
    PROCESSING("Finalizing"),
    COMPLETED("Finished"),
    PAUSED("Paused"),
    CANCELLED("Cancelled"),
    FAILED("Failed")
}

enum class DownloadErrorCategory(val title: String) {
    FORBIDDEN_OR_EXPIRED_URL_403("Stream Expired"),
    UNAUTHORIZED_401("Authorization Required"),
    RATE_LIMITED_429("Server Rate Limited"),
    SERVER_ERROR_5XX("Mirror Server Error"),
    NETWORK_IO_TIMEOUT("Network Timeout"),
    CORRUPTED_STREAM("Corrupted Stream"),
    UNKNOWN("Download Error")
}

data class ClassifiedDownloadError(
    val category: DownloadErrorCategory,
    val userFriendlyMessage: String,
    val recoveryActionHint: String
)
