package com.example.data.scanners

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import com.example.data.models.UsbCapabilityModel

object UsbScanner {

    fun scan(context: Context): UsbCapabilityModel {
        val pm = context.packageManager
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager

        val hasHost = pm.hasSystemFeature("android.hardware.usb.host")
        val hasAcc = pm.hasSystemFeature("android.hardware.usb.accessory")

        val deviceList = try {
            usbManager?.deviceList ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        val names = deviceList.values.map { dev ->
            val name = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                "${dev.productName ?: "USB Device"} (Vid:${dev.vendorId}, Pid:${dev.productId})"
            } else {
                "USB Device (Vid:${dev.vendorId}, Pid:${dev.productId})"
            }
            name
        }

        return UsbCapabilityModel(
            hasUsbHostFeature = hasHost,
            hasUsbAccessoryFeature = hasAcc,
            attachedDevicesCount = deviceList.size,
            attachedDeviceNames = if (names.isNotEmpty()) names else listOf("No external USB devices currently plugged in"),
            supportsUsbAudio = hasHost || pm.hasSystemFeature("android.hardware.audio.output"),
            supportsUsbVideo = hasHost && pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_EXTERNAL),
            supportsOtg = hasHost // USB Host feature on Android corresponds to OTG support
        )
    }
}
