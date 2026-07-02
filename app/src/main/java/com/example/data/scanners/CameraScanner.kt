package com.example.data.scanners

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import android.util.Size
import com.example.data.models.CameraItem

object CameraScanner {

    fun scan(context: Context): List<CameraItem> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return emptyList()
        val items = mutableListOf<CameraItem>()

        val cameraIds = try {
            cameraManager.cameraIdList
        } catch (e: Exception) {
            emptyArray()
        }

        for (id in cameraIds) {
            try {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External USB/UVC"
                    else -> "Unknown Facing"
                }

                val level = when (chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3 (Advanced)"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External USB"
                    else -> "Unknown Level"
                }

                var map: StreamConfigurationMap? = null
                for (k in chars.keys) {
                    if (k.name == "android.scaler.streamConfigurationMap") {
                        @Suppress("UNCHECKED_CAST")
                        map = chars.get(k as CameraCharacteristics.Key<StreamConfigurationMap>)
                        break
                    }
                }
                val sizes: Array<Size>? = map?.getOutputSizes(ImageFormat.JPEG) ?: map?.getOutputSizes(android.graphics.SurfaceTexture::class.java)
                val maxRes = if (sizes != null && sizes.isNotEmpty()) {
                    val max = sizes.maxByOrNull { it.width * it.height }
                    if (max != null) {
                        val mp = (max.width * max.height) / 1000000.0
                        "${max.width}x${max.height} (%.1f MP)".format(mp)
                    } else "Unknown"
                } else "Unknown"

                val isLogical = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    chars.physicalCameraIds.isNotEmpty()
                } else false

                val physicalIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    chars.physicalCameraIds.toList()
                } else emptyList()

                val extensions = mutableListOf<String>()
                val availCaps = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: IntArray(0)
                for (cap in availCaps) {
                    when (cap) {
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW -> extensions.add("RAW Sensor Output")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA -> extensions.add("Logical Multi-Camera")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR -> extensions.add("Manual Sensor Control")
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BURST_CAPTURE -> extensions.add("High Speed Burst")
                    }
                }
                if (extensions.isEmpty()) extensions.add("Standard JPEG/YUV")

                items.add(
                    CameraItem(
                        cameraId = id,
                        facing = facing,
                        resolutionsMegapixels = maxRes,
                        hardwareLevel = level,
                        isLogicalMultiCamera = isLogical,
                        physicalCameraIds = physicalIds,
                        vendorExtensionsSupported = extensions
                    )
                )
            } catch (e: Exception) {
                // Ignore restricted camera ID
            }
        }

        return items
    }
}
