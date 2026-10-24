package com.example.data.service

import com.example.data.model.ClassifiedDownloadError
import com.example.data.model.DownloadErrorCategory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object DownloadErrorHandler {
    fun classify(e: Throwable, providerName: String = "Media"): ClassifiedDownloadError {
        val msg = e.message.orEmpty().lowercase()
        return when {
            msg.contains("403") || msg.contains("forbidden") || msg.contains("expired") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.FORBIDDEN_OR_EXPIRED_URL_403,
                    userFriendlyMessage = "The $providerName direct stream expired or was restricted.",
                    recoveryActionHint = "Try analyzing the link again to generate a fresh stream."
                )
            }
            msg.contains("401") || msg.contains("unauthorized") || msg.contains("login") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.UNAUTHORIZED_401,
                    userFriendlyMessage = "This $providerName content is private or requires authorization.",
                    recoveryActionHint = "Only public reels and videos can be downloaded without login."
                )
            }
            msg.contains("429") || msg.contains("rate limit") || msg.contains("too many requests") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.RATE_LIMITED_429,
                    userFriendlyMessage = "The media server is temporarily rate-limiting requests.",
                    recoveryActionHint = "Wait 30 seconds and retry."
                )
            }
            e is SocketTimeoutException || msg.contains("timeout") || msg.contains("timed out") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.NETWORK_IO_TIMEOUT,
                    userFriendlyMessage = "Network timeout occurred while communicating with the media mirror.",
                    recoveryActionHint = "Check your connection and tap retry."
                )
            }
            e is UnknownHostException || msg.contains("unable to resolve host") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.NETWORK_IO_TIMEOUT,
                    userFriendlyMessage = "Unable to connect to the Internet or resolve media host.",
                    recoveryActionHint = "Ensure your device is connected to Wi-Fi or mobile data."
                )
            }
            msg.contains("html") || msg.contains("truncated") || msg.contains("empty body") -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.CORRUPTED_STREAM,
                    userFriendlyMessage = "The server returned an invalid or incomplete stream.",
                    recoveryActionHint = "LinkFlow will try an alternate stream mirror."
                )
            }
            else -> {
                ClassifiedDownloadError(
                    category = DownloadErrorCategory.UNKNOWN,
                    userFriendlyMessage = e.localizedMessage ?: "An unexpected download error occurred.",
                    recoveryActionHint = "Tap retry or paste the link again."
                )
            }
        }
    }

    fun classifyHttpStatus(statusCode: Int, message: String = "", host: String = ""): ClassifiedDownloadError {
        return when (statusCode) {
            401 -> ClassifiedDownloadError(
                category = DownloadErrorCategory.UNAUTHORIZED_401,
                userFriendlyMessage = "This content is private or requires authorization.",
                recoveryActionHint = "Only public reels and videos can be downloaded without login."
            )
            403 -> ClassifiedDownloadError(
                category = DownloadErrorCategory.FORBIDDEN_OR_EXPIRED_URL_403,
                userFriendlyMessage = "The stream expired or was restricted (HTTP 403).",
                recoveryActionHint = "Try analyzing the link again to generate a fresh stream."
            )
            429 -> ClassifiedDownloadError(
                category = DownloadErrorCategory.RATE_LIMITED_429,
                userFriendlyMessage = "The media server is temporarily rate-limiting requests (HTTP 429).",
                recoveryActionHint = "Wait 30 seconds and retry."
            )
            in 500..599 -> ClassifiedDownloadError(
                category = DownloadErrorCategory.SERVER_ERROR_5XX,
                userFriendlyMessage = "The remote media server encountered an error ($statusCode: $message).",
                recoveryActionHint = "The server is temporarily down. Try again later."
            )
            else -> ClassifiedDownloadError(
                category = DownloadErrorCategory.UNKNOWN,
                userFriendlyMessage = "Download failed with HTTP status $statusCode ($message).",
                recoveryActionHint = "Check the link and retry."
            )
        }
    }
}
