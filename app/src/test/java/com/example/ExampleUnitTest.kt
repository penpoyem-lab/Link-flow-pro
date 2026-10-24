package com.example

import com.example.data.service.MediaAnalyzerEngine
import com.example.data.service.ProviderDetector
import com.example.data.service.UrlAnalysisOutcome
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val engine = MediaAnalyzerEngine()

    @Test
    fun `empty url returns ERR_EMPTY_INPUT`() = runBlocking {
        val outcome = engine.analyzeUrl("   ")
        assertTrue(outcome is UrlAnalysisOutcome.Error)
        val err = outcome as UrlAnalysisOutcome.Error
        assertEquals("ERR_EMPTY_INPUT", err.errorCode)
    }

    @Test
    fun `ssrf localhost and private network urls are blocked`() = runBlocking {
        val localhostOutcome = engine.analyzeUrl("http://127.0.0.1:8080/secret.mp4")
        assertTrue(localhostOutcome is UrlAnalysisOutcome.Error)
        assertEquals("ERR_SSRF_BLOCKED", (localhostOutcome as UrlAnalysisOutcome.Error).errorCode)

        val privateLanOutcome = engine.analyzeUrl("https://192.168.1.50/stream.mp4")
        assertTrue(privateLanOutcome is UrlAnalysisOutcome.Error)
        assertEquals("ERR_SSRF_BLOCKED", (privateLanOutcome as UrlAnalysisOutcome.Error).errorCode)
    }

    @Test
    fun `provider detector correctly identifies platforms`() {
        assertEquals("YouTube", ProviderDetector.detectProvider("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("YouTube", ProviderDetector.detectProvider("https://youtu.be/dQw4w9WgXcQ"))
        assertEquals("Instagram", ProviderDetector.detectProvider("https://www.instagram.com/reel/C7xyz123/"))
        assertEquals("TikTok", ProviderDetector.detectProvider("https://www.tiktok.com/@creator/video/1234567890"))
        assertEquals("Spotify", ProviderDetector.detectProvider("https://open.spotify.com/track/0VjIjW4GlUZAMYd2vXMi3b"))
    }

    @Test
    fun `extract video IDs correctly`() {
        assertEquals("dQw4w9WgXcQ", ProviderDetector.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertEquals("dQw4w9WgXcQ", ProviderDetector.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ?si=abc"))
        assertEquals("C7xyz123", ProviderDetector.extractInstagramCode("https://www.instagram.com/reel/C7xyz123/"))
    }
}
