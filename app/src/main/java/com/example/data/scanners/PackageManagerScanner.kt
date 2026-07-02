package com.example.data.scanners

import android.content.Context
import android.content.pm.PackageManager
import com.example.data.models.PackageFeatureModel

object PackageManagerScanner {

    fun scan(context: Context): List<PackageFeatureModel> {
        val pm = context.packageManager
        val availableFeatures = try {
            pm.systemAvailableFeatures ?: emptyArray()
        } catch (e: Exception) {
            emptyArray()
        }

        val featureModels = mutableListOf<PackageFeatureModel>()

        for (featureInfo in availableFeatures) {
            val name = featureInfo.name ?: continue
            val category = categorizeFeature(name)
            featureModels.add(
                PackageFeatureModel(
                    name = name,
                    category = category,
                    isAvailable = true,
                    version = featureInfo.version
                )
            )
        }

        // Also explicitly test critical standard features in case pm.systemAvailableFeatures misses any edge flags
        val standardFeaturesToTest = listOf(
            "android.hardware.bluetooth" to "Bluetooth",
            "android.hardware.bluetooth_le" to "Bluetooth LE",
            "android.hardware.wifi" to "WiFi",
            "android.hardware.wifi.direct" to "WiFi",
            "android.hardware.wifi.aware" to "WiFi",
            "android.hardware.nfc" to "NFC",
            "android.hardware.camera.any" to "Camera",
            "android.hardware.microphone" to "Microphone",
            "android.hardware.location.gps" to "GPS",
            "android.hardware.usb.host" to "USB",
            "android.hardware.usb.accessory" to "USB",
            "android.hardware.uwb" to "UWB",
            "android.hardware.sensor.accelerometer" to "Sensors",
            "android.hardware.sensor.gyroscope" to "Sensors",
            "android.hardware.telephony" to "Telephony",
            "android.software.leanback" to "TV Features",
            "android.hardware.type.automotive" to "Automotive Features",
            "android.hardware.type.watch" to "Wear Features",
            "android.hardware.audio.output" to "Audio Features",
            "android.hardware.broadcastradio" to "FM Radio / Broadcast",
            "android.hardware.fmradio" to "FM Radio / Broadcast"
        )

        for ((feat, cat) in standardFeaturesToTest) {
            if (featureModels.none { it.name == feat }) {
                val hasFeat = pm.hasSystemFeature(feat)
                featureModels.add(
                    PackageFeatureModel(
                        name = feat,
                        category = cat,
                        isAvailable = hasFeat,
                        version = 0
                    )
                )
            }
        }

        return featureModels.sortedBy { it.category }
    }

    private fun categorizeFeature(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("fm") || lower.contains("broadcast") || lower.contains("tuner") -> "FM Radio / Broadcast"
            lower.contains("bluetooth_le") || lower.contains("ble") -> "Bluetooth LE"
            lower.contains("bluetooth") -> "Bluetooth"
            lower.contains("wifi") || lower.contains("wlan") -> "WiFi"
            lower.contains("nfc") -> "NFC"
            lower.contains("camera") -> "Camera"
            lower.contains("mic") || lower.contains("audio") -> "Audio Features"
            lower.contains("gps") || lower.contains("location") -> "GPS"
            lower.contains("usb") || lower.contains("otg") -> "USB"
            lower.contains("uwb") -> "UWB"
            lower.contains("sensor") -> "Sensors"
            lower.contains("telephony") || lower.contains("gsm") || lower.contains("cdma") -> "Telephony"
            lower.contains("tv") || lower.contains("leanback") -> "TV Features"
            lower.contains("automotive") -> "Automotive Features"
            lower.contains("watch") || lower.contains("wear") -> "Wear Features"
            lower.startsWith("android.hardware") -> "Hardware Features"
            lower.startsWith("android.software") -> "Software Features"
            else -> "Other System Features"
        }
    }
}
