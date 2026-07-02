package com.example.data.scanners

import com.example.data.models.SystemPropertyItem
import java.io.BufferedReader
import java.io.InputStreamReader

object SystemPropertyScanner {

    private val HIGHLIGHT_KEYWORDS = listOf(
        "fm", "radio", "rf", "tv", "qualcomm", "qcom", "vendor", "audio", "dsp", "media", "tuner", "broadcast"
    )

    fun scan(): List<SystemPropertyItem> {
        val properties = mutableMapOf<String, String>()

        // Method 1: Execute getprop safely without ADB or root
        try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()
            val regex = """\[(.*?)\]:\s*\[(.*?)\]""".toRegex()
            while (line != null) {
                val match = regex.find(line)
                if (match != null && match.groupValues.size >= 3) {
                    val key = match.groupValues[1]
                    val value = match.groupValues[2]
                    properties[key] = value
                } else if (line.contains(":")) {
                    val parts = line.split(":", limit = 2)
                    if (parts.size == 2) {
                        properties[parts[0].trim('[', ']', ' ')] = parts[1].trim('[', ']', ' ')
                    }
                }
                line = reader.readLine()
            }
            reader.close()
            process.destroy()
        } catch (e: Exception) {
            // Fallback if exec is restricted by SELinux on Android 14/15/16
        }

        // Method 2: Inspect well-known properties via reflection if getprop returned few items
        if (properties.size < 20) {
            val knownKeys = listOf(
                "ro.build.version.release", "ro.build.version.sdk", "ro.product.model",
                "ro.product.brand", "ro.product.manufacturer", "ro.product.device",
                "ro.board.platform", "ro.hardware", "ro.arch",
                "vendor.audio.feature.fm.enable", "ro.fm.chip.port.FM_OUT",
                "ro.vendor.fm.use_audio_session", "media.fm.radio",
                "ro.qualcomm.crashed", "ro.vendor.qti.core.factory",
                "persist.vendor.radio.adb_log_on", "ro.telephony.default_network",
                "vendor.audio.tunnel.encode", "vendor.audio.offload.buffer.size.kb",
                "ro.vendor.audio.sdk.fluencetype", "vendor.media.omx", "ro.vendor.media.video.vpp"
            )
            for (key in knownKeys) {
                if (!properties.containsKey(key)) {
                    val valStr = getSystemProperty(key)
                    if (valStr.isNotEmpty()) {
                        properties[key] = valStr
                    }
                }
            }
        }

        val items = mutableListOf<SystemPropertyItem>()
        for ((key, value) in properties) {
            val keyLower = key.lowercase()
            val valLower = value.lowercase()
            val isHighlighted = HIGHLIGHT_KEYWORDS.any { keyLower.contains(it) || valLower.contains(it) }
            val category = when {
                keyLower.contains("fm") || keyLower.contains("broadcast") || keyLower.contains("tuner") -> "FM / Broadcast"
                keyLower.contains("radio") || keyLower.contains("rf") || keyLower.contains("ril") -> "Radio / RF"
                keyLower.contains("qualcomm") || keyLower.contains("qcom") || keyLower.contains("qti") -> "Qualcomm Hardware"
                keyLower.contains("audio") || keyLower.contains("dsp") || keyLower.contains("mixer") -> "Audio / DSP"
                keyLower.contains("media") || keyLower.contains("codec") || keyLower.contains("omx") -> "Media Codecs"
                keyLower.contains("tv") || keyLower.contains("dvb") -> "TV Tuner"
                keyLower.startsWith("ro.build") || keyLower.startsWith("ro.product") -> "System Build"
                else -> "General Property"
            }
            items.add(SystemPropertyItem(key, value, isHighlighted, category))
        }

        return items.sortedWith(compareByDescending<SystemPropertyItem> { it.isHighlighted }.thenBy { it.key })
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val method = clazz.getMethod("get", String::class.java, String::class.java)
            method.invoke(null, key, "") as String
        } catch (e: Exception) {
            ""
        }
    }
}
