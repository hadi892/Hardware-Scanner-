package com.example.data.scanners

import android.content.Context
import android.content.pm.PackageManager
import com.example.data.models.CapabilityStatus
import com.example.data.models.HalCapabilityItem

object HalScanner {

    fun scan(context: Context): List<HalCapabilityItem> {
        val pm = context.packageManager
        val hals = mutableListOf<HalCapabilityItem>()

        val targetHals = listOf(
            Triple("android.hardware.audio", "Audio HAL", "Audio output/input hardware abstraction"),
            Triple("android.hardware.bluetooth", "Bluetooth HAL", "Bluetooth Core & LE Radio interface"),
            Triple("android.hardware.camera.any", "Camera HAL", "ISP & Camera sensor HAL pipeline"),
            Triple("android.hardware.broadcastradio", "Radio HAL", "Broadcast FM Radio / AM Hardware HAL"),
            Triple("android.software.leanback", "TV HAL", "Android TV & HDMI CEC Framework"),
            Triple("android.hardware.tv.tuner", "Tuner HAL", "Digital TV ATSC/DVB Hardware Tuner HAL"),
            Triple("android.hardware.media", "Media HAL", "Hardware accelerated media processing")
        )

        for ( (feature, category, desc) in targetHals ) {
            val hasFeature = pm.hasSystemFeature(feature)
            val status = if (hasFeature) CapabilityStatus.CONFIRMED else {
                // Check if vendor libraries exist to see if it's restricted or hidden
                if (category == "Radio HAL" || category == "Tuner HAL") {
                    CapabilityStatus.SUSPICIOUS_EVIDENCE
                } else {
                    CapabilityStatus.NOT_PRESENT
                }
            }
            hals.add(
                HalCapabilityItem(
                    halName = feature,
                    category = category,
                    versionOrInfo = if (hasFeature) "HAL Present ($desc)" else "Feature flag not exposed ($desc)",
                    status = status
                )
            )
        }

        return hals
    }
}
