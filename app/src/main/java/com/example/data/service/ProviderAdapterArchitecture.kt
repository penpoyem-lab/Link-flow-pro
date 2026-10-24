package com.example.data.service

import java.io.File
import java.io.FileInputStream

data class MediaFileValidationResult(
    val isValid: Boolean,
    val isHtmlWebpage: Boolean = false,
    val reason: String? = null,
    val detectedFormat: String = "UNKNOWN"
)

object MediaStreamValidator {
    private val HTML_SIGNATURE_PREFIXES = listOf(
        "<!doctype html",
        "<html",
        "<?xml",
        "{\"error\"",
        "{\"status\":\"error\"",
        "{\"message\":"
    )

    fun isInvalidNonMediaContentType(cType: String): Boolean {
        val lower = cType.lowercase()
        return lower.startsWith("text/html") ||
                lower.startsWith("application/json") ||
                lower.startsWith("text/plain")
    }

    fun isLikelyWebpageLandingUrl(url: String): Boolean {
        val lower = url.lowercase()
        if (lower.contains("googlevideo.com") || lower.contains(".cdninstagram.com") || lower.contains("fbcdn.net") || lower.contains("savenow.to")) {
            return false
        }
        if (lower.contains("youtube.com/watch") ||
            lower.contains("youtu.be/") ||
            lower.contains("instagram.com/reel/") ||
            lower.contains("instagram.com/p/")
        ) {
            return true
        }
        return false
    }

    fun validateDownloadedMediaFile(file: File): MediaFileValidationResult {
        if (!file.exists()) {
            return MediaFileValidationResult(isValid = false, reason = "Downloaded file does not exist on disk")
        }
        val length = file.length()
        if (length <= 512L) {
            return MediaFileValidationResult(isValid = false, reason = "Downloaded stream was empty or truncated ($length B)")
        }

        return try {
            val headerBytes = ByteArray(512)
            val readCount = FileInputStream(file).use { it.read(headerBytes) }
            if (readCount <= 0) {
                return MediaFileValidationResult(isValid = false, reason = "Unable to read media header bytes")
            }

            val headAscii = String(headerBytes, 0, readCount, Charsets.UTF_8).trimStart().lowercase()

            for (prefix in HTML_SIGNATURE_PREFIXES) {
                if (headAscii.startsWith(prefix)) {
                    return MediaFileValidationResult(
                        isValid = false,
                        isHtmlWebpage = true,
                        reason = "Server returned an HTML webpage or JSON error instead of a binary media stream"
                    )
                }
            }

            if (headAscii.contains("<html") || headAscii.contains("<!doctype html")) {
                return MediaFileValidationResult(
                    isValid = false,
                    isHtmlWebpage = true,
                    reason = "Server returned an HTML login/consent page instead of a binary media stream"
                )
            }

            // Inspect binary magic signatures
            val detectedFormat = when {
                readCount >= 8 &&
                        headerBytes[4] == 'f'.code.toByte() &&
                        headerBytes[5] == 't'.code.toByte() &&
                        headerBytes[6] == 'y'.code.toByte() &&
                        headerBytes[7] == 'p'.code.toByte() -> "MP4/M4A"
                readCount >= 4 &&
                        headerBytes[0] == 'f'.code.toByte() &&
                        headerBytes[1] == 'L'.code.toByte() &&
                        headerBytes[2] == 'a'.code.toByte() &&
                        headerBytes[3] == 'C'.code.toByte() -> "FLAC"
                readCount >= 3 &&
                        headerBytes[0] == 'I'.code.toByte() &&
                        headerBytes[1] == 'D'.code.toByte() &&
                        headerBytes[2] == '3'.code.toByte() -> "MP3"
                readCount >= 2 &&
                        (headerBytes[0].toInt() and 0xFF) == 0xFF &&
                        ((headerBytes[1].toInt() and 0xE0) == 0xE0) -> "MP3/AAC"
                readCount >= 4 &&
                        (headerBytes[0].toInt() and 0xFF) == 0x1A &&
                        (headerBytes[1].toInt() and 0xFF) == 0x45 &&
                        (headerBytes[2].toInt() and 0xFF) == 0xDF &&
                        (headerBytes[3].toInt() and 0xFF) == 0xA3 -> "WEBM/MKV"
                readCount >= 4 &&
                        headerBytes[0] == 'R'.code.toByte() &&
                        headerBytes[1] == 'I'.code.toByte() &&
                        headerBytes[2] == 'F'.code.toByte() &&
                        headerBytes[3] == 'F'.code.toByte() -> "WAV"
                readCount >= 4 &&
                        headerBytes[0] == 'O'.code.toByte() &&
                        headerBytes[1] == 'g'.code.toByte() &&
                        headerBytes[2] == 'g'.code.toByte() &&
                        headerBytes[3] == 'S'.code.toByte() -> "OGG"
                else -> "BINARY_MEDIA"
            }

            MediaFileValidationResult(isValid = true, detectedFormat = detectedFormat)
        } catch (e: Exception) {
            MediaFileValidationResult(isValid = false, reason = "Validation exception: ${e.message}")
        }
    }
}
