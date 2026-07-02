package com.example.data.scanners

import com.example.data.models.BinderServiceItem
import java.io.BufferedReader
import java.io.InputStreamReader

object BinderScanner {

    fun scan(): List<BinderServiceItem> {
        val serviceNames = mutableSetOf<String>()
        val serviceInterfaces = mutableMapOf<String, String>()

        // Method 1: Reflection on ServiceManager.listServices()
        try {
            val smClass = Class.forName("android.os.ServiceManager")
            val listMethod = smClass.getMethod("listServices")
            val list = listMethod.invoke(null) as? Array<String>
            if (list != null) {
                serviceNames.addAll(list)
            }
        } catch (e: Exception) {
            // Restricted
        }

        // Method 2: Execute service list
        try {
            val proc = Runtime.getRuntime().exec("service list")
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            var line = reader.readLine()
            val regex = """^\d+:\s+\[(.*?)\]:\s+\[(.*?)\]""".toRegex()
            while (line != null) {
                val match = regex.find(line)
                if (match != null && match.groupValues.size >= 3) {
                    val name = match.groupValues[1]
                    val iface = match.groupValues[2]
                    serviceNames.add(name)
                    serviceInterfaces[name] = iface
                } else if (line.contains(":")) {
                    val parts = line.split(":")
                    if (parts.size >= 2) {
                        val name = parts[1].substringBefore("[").trim()
                        if (name.isNotEmpty()) serviceNames.add(name)
                    }
                }
                line = reader.readLine()
            }
            reader.close()
            proc.destroy()
        } catch (e: Exception) {
            // Ignore
        }

        // Fallback standard critical services to check if shell command was blocked
        if (serviceNames.isEmpty()) {
            serviceNames.addAll(
                listOf(
                    "audio", "media.player", "media.audio_flinger", "media.camera",
                    "broadcastradio", "tv_tuner", "phone", "bluetooth_manager",
                    "sensorservice", "wifi", "location", "usb"
                )
            )
        }

        val items = mutableListOf<BinderServiceItem>()
        for (name in serviceNames.sorted()) {
            val lower = name.lowercase()
            val isSuspicious = lower.contains("fm") || lower.contains("radio") ||
                    lower.contains("tuner") || lower.contains("tv") ||
                    lower.contains("media") || lower.contains("audio")

            val purpose = guessPurpose(name)
            items.add(
                BinderServiceItem(
                    serviceName = name,
                    interfaceDescriptor = serviceInterfaces[name] ?: guessInterface(name),
                    isAccessible = checkServiceAccessible(name),
                    purposeGuess = purpose,
                    isSuspiciousMultimedia = isSuspicious
                )
            )
        }

        return items.sortedWith(compareByDescending<BinderServiceItem> { it.isSuspiciousMultimedia }.thenBy { it.serviceName })
    }

    private fun checkServiceAccessible(serviceName: String): Boolean {
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val checkServiceMethod = smClass.getMethod("checkService", String::class.java)
            val binder = checkServiceMethod.invoke(null, serviceName)
            binder != null
        } catch (e: Exception) {
            false
        }
    }

    private fun guessInterface(name: String): String {
        return "android.os.IBinder ($name)"
    }

    private fun guessPurpose(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.contains("fm") || lower.contains("broadcastradio") -> "FM Radio / Broadcast HAL Interface"
            lower.contains("tuner") || lower.contains("tv") -> "Hardware TV / Tuner Pipeline"
            lower.contains("audio") || lower.contains("flinger") -> "System Audio Management & DSP Routing"
            lower.contains("media") || lower.contains("codec") -> "Multimedia Decoding / Encoding Framework"
            lower.contains("camera") -> "Hardware Camera Subsystem"
            lower.contains("phone") || lower.contains("ril") -> "Telephony & Radio Interface Layer"
            lower.contains("bluetooth") -> "Bluetooth Wireless Subsystem"
            lower.contains("sensor") -> "Sensor Hub Management"
            else -> "Android Core System Service"
        }
    }
}
