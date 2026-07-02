package com.example.data.scanners

import com.example.data.models.ConfigFileItem
import java.io.File
import java.util.Scanner

object ConfigScanner {

    private val TARGET_CONFIG_PATHS = listOf(
        "/vendor/etc/audio_policy_configuration.xml",
        "/vendor/etc/audio_effects.xml",
        "/vendor/etc/mixer_paths.xml",
        "/vendor/etc/mixer_paths_qvr.xml",
        "/vendor/etc/media_codecs.xml",
        "/vendor/etc/media_profiles_V1_0.xml",
        "/system/etc/audio_policy_configuration.xml",
        "/system/etc/media_codecs.xml",
        "/odm/etc/audio_policy_configuration.xml",
        "/vendor/etc/vintf/manifest.xml",
        "/system/etc/vintf/manifest.xml"
    )

    private val KEYWORDS = listOf(
        "FM", "Radio", "Broadcast", "Tuner", "RF", "Qualcomm", "AudioPolicy", "Mixer", "DSP", "Vendor", "HAL"
    )

    fun scan(): List<ConfigFileItem> {
        val results = mutableListOf<ConfigFileItem>()

        for (path in TARGET_CONFIG_PATHS) {
            val file = File(path)
            val exists = file.exists()
            val canRead = file.canRead()

            if (exists && canRead && file.isFile) {
                try {
                    val contentExcerpt = StringBuilder()
                    val matchedKeywords = mutableSetOf<String>()
                    var lineCount = 0

                    file.useLines { lines ->
                        for (line in lines) {
                            for (kw in KEYWORDS) {
                                if (line.contains(kw, ignoreCase = true)) {
                                    matchedKeywords.add(kw)
                                    if (contentExcerpt.length < 350) {
                                        contentExcerpt.append(line.trim()).append("\n")
                                    }
                                }
                            }
                            lineCount++
                            if (lineCount > 3000) break
                        }
                    }

                    val kwStr = if (matchedKeywords.isNotEmpty()) matchedKeywords.joinToString(", ") else "Standard Config"
                    results.add(
                        ConfigFileItem(
                            path = path,
                            keywordMatched = kwStr,
                            previewExcerpt = if (contentExcerpt.isNotEmpty()) contentExcerpt.toString() else "(File accessible, standard definitions found)",
                            isReadable = true
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        ConfigFileItem(
                            path = path,
                            keywordMatched = "Error reading",
                            previewExcerpt = e.localizedMessage ?: "Unknown Read Error",
                            isReadable = false
                        )
                    )
                }
            } else {
                results.add(
                    ConfigFileItem(
                        path = path,
                        keywordMatched = "Restricted / Missing",
                        previewExcerpt = if (!exists) "File not present on this build." else "File exists but restricted by SELinux permissions.",
                        isReadable = false
                    )
                )
            }
        }

        return results
    }
}
