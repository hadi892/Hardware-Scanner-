package com.example.data.scanners

import com.example.data.models.LibraryItem
import java.io.File

object LibraryScanner {

    private val SEARCH_DIRS = listOf(
        "/system/lib",
        "/system/lib64",
        "/vendor/lib",
        "/vendor/lib64",
        "/system_ext/lib",
        "/system_ext/lib64",
        "/product/lib",
        "/product/lib64",
        "/odm/lib",
        "/odm/lib64",
        "/apex"
    )

    private val TARGET_KEYWORDS = listOf(
        "fm", "radio", "qcom", "qualcomm", "rf", "dsp", "audio", "media",
        "broadcast", "tv", "dvb", "atsc", "isdb", "tuner", "decoder",
        "encoder", "frontend", "demod"
    )

    fun scan(): List<LibraryItem> {
        val foundLibraries = mutableListOf<LibraryItem>()
        val seenPaths = mutableSetOf<String>()

        for (dirPath in SEARCH_DIRS) {
            val dir = File(dirPath)
            if (dir.exists() && dir.canRead() && dir.isDirectory) {
                scanDirectoryRecursively(dir, foundLibraries, seenPaths, maxDepth = 3)
            }
        }

        // Always check specific suspicious drivers/libraries directly even if parent dir listing is restricted
        val explicitTargets = listOf(
            "/system/lib64/libfmjni.so",
            "/system/lib64/libqcomfm_jni.so",
            "/vendor/lib64/libqcomfm_jni.so",
            "/vendor/lib64/hw/audio.r_submix.default.so",
            "/vendor/lib64/hw/audio.primary.default.so",
            "/system/lib64/libstagefright.so",
            "/vendor/lib64/libqservice.so",
            "/vendor/lib64/libqmiservices.so",
            "/vendor/lib64/libadsprpc.so",
            "/vendor/lib64/libcdsprpc.so",
            "/vendor/lib64/libsdmcore.so"
        )

        for (path in explicitTargets) {
            if (!seenPaths.contains(path)) {
                val f = File(path)
                val exists = f.exists()
                val nameLower = f.name.lowercase()
                val keyword = TARGET_KEYWORDS.firstOrNull { nameLower.contains(it) } ?: "system_lib"
                if (exists || TARGET_KEYWORDS.any { nameLower.contains(it) }) {
                    foundLibraries.add(
                        LibraryItem(
                            path = path,
                            fileName = f.name,
                            category = categorizeLibrary(f.name),
                            existsOnDisk = exists,
                            sizeBytes = if (exists && f.isFile) try { f.length() } catch (e: Exception) { 0L } else 0L
                        )
                    )
                    seenPaths.add(path)
                }
            }
        }

        return foundLibraries.sortedWith(compareByDescending<LibraryItem> { it.existsOnDisk }.thenBy { it.category })
    }

    private fun scanDirectoryRecursively(
        dir: File,
        results: MutableList<LibraryItem>,
        seenPaths: MutableSet<String>,
        maxDepth: Int
    ) {
        if (maxDepth <= 0 || results.size > 800) return
        val files = try {
            dir.listFiles()
        } catch (e: Exception) {
            null
        } ?: return

        for (file in files) {
            val path = file.absolutePath
            if (seenPaths.contains(path)) continue

            if (file.isDirectory) {
                scanDirectoryRecursively(file, results, seenPaths, maxDepth - 1)
            } else if (file.isFile) {
                val nameLower = file.name.lowercase()
                if (TARGET_KEYWORDS.any { nameLower.contains(it) }) {
                    seenPaths.add(path)
                    results.add(
                        LibraryItem(
                            path = path,
                            fileName = file.name,
                            category = categorizeLibrary(file.name),
                            existsOnDisk = true,
                            sizeBytes = try { file.length() } catch (e: Exception) { 0L }
                        )
                    )
                }
            }
        }
    }

    private fun categorizeLibrary(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("fm") -> "FM Radio / Broadcast"
            lower.contains("qcom") || lower.contains("qualcomm") || lower.contains("adsp") || lower.contains("cdsp") -> "Qualcomm DSP/RF"
            lower.contains("radio") || lower.contains("ril") || lower.contains("rf") -> "Radio Interface"
            lower.contains("tv") || lower.contains("dvb") || lower.contains("atsc") || lower.contains("isdb") || lower.contains("tuner") -> "Digital TV Tuner"
            lower.contains("audio") || lower.contains("mixer") -> "Audio Subsystem"
            lower.contains("decoder") || lower.contains("encoder") || lower.contains("media") -> "Media Codec"
            else -> "System Library"
        }
    }
}
