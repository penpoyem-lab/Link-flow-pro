package com.example.data.service

import org.json.JSONObject

data class ApkAssetInfo(
    val name: String,
    val downloadUrl: String,
    val sizeBytes: Long,
    val sha256: String?
)

data class ParsedRelease(
    val cleanVersionName: String,
    val releaseTitle: String,
    val apkAsset: ApkAssetInfo,
    val topFeatures: List<String>,
    val htmlUrl: String,
    val publishedAt: String
)

object AppUpdateManager {

    fun parseGithubReleaseJson(
        jsonString: String,
        repoName: String,
        currentVersion: String
    ): ParsedRelease? {
        return runCatching {
            val root = JSONObject(jsonString)
            if (root.optBoolean("draft", false)) return null
            val tagName = root.optString("tag_name", "").trim()
            val cleanVer = tagName.removePrefix("v").trim()
            if (cleanVer.isBlank()) return null

            val body = root.optString("body", "")
            val topFeatures = mutableListOf<String>()
            body.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    topFeatures.add(trimmed.removePrefix("- ").removePrefix("* ").trim())
                }
            }

            val assets = root.optJSONArray("assets") ?: return null
            var chosenApk: ApkAssetInfo? = null
            for (i in 0 until assets.length()) {
                val a = assets.optJSONObject(i) ?: continue
                val name = a.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    val dl = a.optString("browser_download_url", "")
                    val sz = a.optLong("size", 0L)
                    val digest = a.optString("digest", "").ifBlank { null }
                    chosenApk = ApkAssetInfo(
                        name = name,
                        downloadUrl = dl,
                        sizeBytes = sz,
                        sha256 = digest
                    )
                    break
                }
            }

            if (chosenApk == null) return null

            ParsedRelease(
                cleanVersionName = cleanVer,
                releaseTitle = root.optString("name", "Version $cleanVer"),
                apkAsset = chosenApk,
                topFeatures = topFeatures,
                htmlUrl = root.optString("html_url", ""),
                publishedAt = root.optString("published_at", "")
            )
        }.getOrNull()
    }
}
