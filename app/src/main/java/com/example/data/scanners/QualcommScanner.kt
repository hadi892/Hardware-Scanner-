package com.example.data.scanners

import com.example.data.models.LibraryItem
import com.example.data.models.QualcommComponentModel
import com.example.data.models.SystemPropertyItem

object QualcommScanner {

    fun scan(scannedLibs: List<LibraryItem>, scannedProps: List<SystemPropertyItem>): List<QualcommComponentModel> {
        val components = mutableListOf<QualcommComponentModel>()

        // Analyze libraries
        for (lib in scannedLibs) {
            val nameLower = lib.fileName.lowercase()
            when {
                nameLower.contains("adsp") || nameLower.contains("cdsp") || nameLower.contains("hexagon") -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "DSP / Hexagon",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium (Referenced in Manifest/Path)"
                        )
                    )
                }
                nameLower.contains("qmi") -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "QMI (Qualcomm Messaging Interface)",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium"
                        )
                    )
                }
                nameLower.contains("diag") -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "Diag / Diagnostics Interface",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium"
                        )
                    )
                }
                nameLower.contains("ril") || nameLower.contains("qcril") -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "RIL (Radio Interface Layer)",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium"
                        )
                    )
                }
                nameLower.contains("qcomfm") || nameLower.contains("fm") -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "RF / FM Radio Component",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium"
                        )
                    )
                }
                nameLower.contains("audio") && (nameLower.contains("qcom") || lib.path.contains("qcom")) -> {
                    components.add(
                        QualcommComponentModel(
                            name = lib.fileName,
                            category = "Audio DSP",
                            detectedLocation = lib.path,
                            confidence = if (lib.existsOnDisk) "High (File Found on Disk)" else "Medium"
                        )
                    )
                }
            }
        }

        // Analyze properties
        for (prop in scannedProps) {
            val keyLower = prop.key.lowercase()
            val valLower = prop.value.lowercase()
            if (keyLower.contains("qualcomm") || keyLower.contains("qcom") || keyLower.contains("qti")) {
                val cat = when {
                    keyLower.contains("audio") || keyLower.contains("dsp") -> "Audio DSP Property"
                    keyLower.contains("radio") || keyLower.contains("ril") || keyLower.contains("modem") -> "Modem Components"
                    keyLower.contains("diag") -> "Diag Property"
                    else -> "System SoC Property"
                }
                if (components.none { it.name == prop.key }) {
                    components.add(
                        QualcommComponentModel(
                            name = prop.key,
                            category = cat,
                            detectedLocation = "SystemProperty [${prop.value}]",
                            confidence = "Confirmed Active Property"
                        )
                    )
                }
            }
        }

        // Always ensure we inspect standard SoC platform identity
        val board = android.os.Build.BOARD
        val hardware = android.os.Build.HARDWARE
        if (board.contains("qcom", true) || hardware.contains("qcom", true) ||
            board.contains("sm", true) || board.contains("sdm", true) || board.contains("bengal", true) ||
            board.contains("taro", true) || board.contains("kalama", true)) {
            components.add(0,
                QualcommComponentModel(
                    name = "SoC Core Platform (${android.os.Build.BOARD})",
                    category = "Modem Components & Core SoC",
                    detectedLocation = "Build.BOARD / Build.HARDWARE",
                    confidence = "100% Confirmed Snapdragon/Qualcomm Platform Architecture"
                )
            )
        }

        return components.distinctBy { "${it.name}_${it.detectedLocation}" }
    }
}
