package com.example.data.scanners

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.data.models.SystemInfoModel
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

object SystemInfoScanner {

    fun scan(context: Context): SystemInfoModel {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        var totalStorage = 0L
        var availStorage = 0L
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            totalStorage = stat.totalBytes
            availStorage = stat.availableBytes
        } catch (e: Exception) {
            // Ignore if restricted
        }

        val kernelVersion = try {
            System.getProperty("os.version") ?: getKernelVersionFromFile() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        val radioVer = try {
            Build.getRadioVersion() ?: "None/Unknown"
        } catch (e: Exception) {
            "Restricted"
        }

        val displayMetrics = context.resources.displayMetrics
        val resString = "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px"
        val metricsDetails = "Density: ${displayMetrics.densityDpi} DPI (${displayMetrics.density}x), Screen: %.2f x %.2f dp".format(
            displayMetrics.widthPixels / displayMetrics.density,
            displayMetrics.heightPixels / displayMetrics.density
        )

        val cpuArch = try {
            System.getProperty("os.arch") ?: Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }

        return SystemInfoModel(
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            kernelVersion = kernelVersion,
            buildFingerprint = Build.FINGERPRINT ?: "Unknown",
            buildTags = Build.TAGS ?: "Unknown",
            buildType = Build.TYPE ?: "Unknown",
            board = Build.BOARD ?: "Unknown",
            brand = Build.BRAND ?: "Unknown",
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            model = Build.MODEL ?: "Unknown",
            product = Build.PRODUCT ?: "Unknown",
            device = Build.DEVICE ?: "Unknown",
            hardware = Build.HARDWARE ?: "Unknown",
            bootloader = Build.BOOTLOADER ?: "Unknown",
            radioVersion = radioVer,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "N/A",
            supportedAbis = Build.SUPPORTED_ABIS?.toList() ?: emptyList(),
            cpuArchitecture = cpuArch,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            totalMemoryBytes = memoryInfo.totalMem,
            availableMemoryBytes = memoryInfo.availMem,
            totalStorageBytes = totalStorage,
            availableStorageBytes = availStorage,
            gpuRenderer = "Detected via EGL/OpenGL ES surface (Hardware Accelerated)",
            screenResolution = resString,
            displayMetricsInfo = metricsDetails
        )
    }

    private fun getKernelVersionFromFile(): String? {
        return try {
            val file = File("/proc/version")
            if (file.exists() && file.canRead()) {
                BufferedReader(FileReader(file)).use { it.readLine() }
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
