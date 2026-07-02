package com.example.data.scanners

import android.media.MediaCodecList
import android.os.Build
import com.example.data.models.CodecItem

object CodecScanner {

    fun scan(): List<CodecItem> {
        val codecItems = mutableListOf<CodecItem>()
        try {
            val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
            val infos = codecList.codecInfos

            for (info in infos) {
                val name = info.name
                val types = info.supportedTypes.toList()
                val isEncoder = info.isEncoder
                val isHardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    info.isHardwareAccelerated
                } else {
                    !name.startsWith("OMX.google.") && !name.startsWith("c2.android.") && !name.contains(".sw.")
                }
                val isVendor = !name.startsWith("OMX.google.") && !name.startsWith("c2.android.")

                codecItems.add(
                    CodecItem(
                        name = name,
                        supportedTypes = types,
                        isHardwareAccelerated = isHardware,
                        isEncoder = isEncoder,
                        isVendorSpecific = isVendor
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
        return codecItems.sortedBy { it.name }
    }
}
