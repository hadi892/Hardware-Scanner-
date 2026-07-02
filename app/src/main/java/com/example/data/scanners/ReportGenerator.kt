package com.example.data.scanners

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.models.FullHardwareAnalysisReport
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    fun generateHtmlReport(report: FullHardwareAnalysisReport): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.scanTimestamp))
        val sb = StringBuilder()
        sb.append("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Hardware Capability Analyzer Ultimate - Report</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #0D1117; color: #E6EDF3; margin: 0; padding: 20px; }
                    .header { background: #161B22; border: 1px solid #30363D; border-radius: 8px; padding: 20px; margin-bottom: 24px; }
                    h1 { color: #58A6FF; margin-top: 0; }
                    h2 { color: #79C0FF; border-bottom: 1px solid #30363D; padding-bottom: 8px; margin-top: 32px; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px; margin-bottom: 24px; }
                    .card { background: #161B22; border: 1px solid #30363D; border-radius: 8px; padding: 16px; }
                    .badge { display: inline-block; padding: 4px 8px; border-radius: 12px; font-size: 12px; font-weight: bold; }
                    .badge-high { background: #3FB950; color: #0D1117; }
                    .badge-med { background: #D29922; color: #0D1117; }
                    .badge-low { background: #F85149; color: #FFFFFF; }
                    table { width: 100%; border-collapse: collapse; margin-top: 12px; background: #161B22; border-radius: 8px; overflow: hidden; }
                    th, td { padding: 12px; text-align: left; border-bottom: 1px solid #30363D; font-size: 14px; }
                    th { background: #21262D; color: #8B949E; }
                    tr:hover { background: #1F242D; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>⚡ Hardware Capability Analyzer Ultimate</h1>
                    <p><strong>Target Device:</strong> ${report.deviceTitle} (${report.systemInfo.manufacturer} ${report.systemInfo.model})</p>
                    <p><strong>Android OS:</strong> Android ${report.systemInfo.androidVersion} (API ${report.systemInfo.apiLevel}) | <strong>Scan Date:</strong> $dateStr</p>
                    <p><strong>SoC Architecture:</strong> ${report.systemInfo.board} (${report.systemInfo.hardware}) - ${report.systemInfo.cpuArchitecture}</p>
                </div>
        """.trimIndent())

        // Hidden Capability Confidence Scores
        sb.append("<h2>🔬 Module 15 & 16: Hidden Capability & Risk Matrix</h2>")
        sb.append("<div class=\"grid\">")
        for (score in report.hiddenCapabilityScores) {
            val badgeClass = when {
                score.confidencePercentage >= 65 -> "badge-high"
                score.confidencePercentage >= 35 -> "badge-med"
                else -> "badge-low"
            }
            sb.append("""
                <div class="card">
                    <h3 style="margin-top:0; color:#E6EDF3;">${score.capabilityName}</h3>
                    <p><span class="badge $badgeClass">${score.confidencePercentage}% Confidence</span></p>
                    <p style="font-size: 13px; color: #8B949E;"><strong>Assessment:</strong> ${score.overallAssessment}</p>
                    <p style="font-size: 12px; color: #A5D6FF;"><strong>Evidence Found:</strong> ${score.evidenceFound.joinToString("; ")}</p>
                </div>
            """.trimIndent())
        }
        sb.append("</div>")

        // System Info Table
        sb.append("<h2>📊 Module 1: System Specifications</h2>")
        sb.append("<table><tr><th>Property</th><th>Value</th></tr>")
        sb.append("<tr><td>Manufacturer & Brand</td><td>${report.systemInfo.manufacturer} / ${report.systemInfo.brand}</td></tr>")
        sb.append("<tr><td>Model & Product</td><td>${report.systemInfo.model} (${report.systemInfo.product})</td></tr>")
        sb.append("<tr><td>Board & Hardware</td><td>${report.systemInfo.board} / ${report.systemInfo.hardware}</td></tr>")
        sb.append("<tr><td>Kernel Version</td><td>${report.systemInfo.kernelVersion}</td></tr>")
        sb.append("<tr><td>Radio Baseband</td><td>${report.systemInfo.radioVersion}</td></tr>")
        sb.append("<tr><td>CPU Architecture & Cores</td><td>${report.systemInfo.cpuArchitecture} (${report.systemInfo.cpuCores} Cores)</td></tr>")
        sb.append("<tr><td>Memory (RAM)</td><td>%.2f GB Available / %.2f GB Total</td></tr>".format(report.systemInfo.availableMemoryBytes / 1e9, report.systemInfo.totalMemoryBytes / 1e9))
        sb.append("<tr><td>Storage</td><td>%.2f GB Available / %.2f GB Total</td></tr>".format(report.systemInfo.availableStorageBytes / 1e9, report.systemInfo.totalStorageBytes / 1e9))
        sb.append("<tr><td>Display Resolution</td><td>${report.systemInfo.screenResolution} (${report.systemInfo.displayMetricsInfo})</td></tr>")
        sb.append("</table>")

        // Qualcomm Table
        if (report.qualcommComponents.isNotEmpty()) {
            sb.append("<h2>⚙️ Module 14: Qualcomm Architecture Components</h2>")
            sb.append("<table><tr><th>Component Name</th><th>Category</th><th>Location</th><th>Confidence</th></tr>")
            for (qc in report.qualcommComponents) {
                sb.append("<tr><td>${qc.name}</td><td>${qc.category}</td><td><code>${qc.detectedLocation}</code></td><td>${qc.confidence}</td></tr>")
            }
            sb.append("</table>")
        }

        // PackageManager Features
        sb.append("<h2>📦 Module 2: PackageManager System Features (${report.packageFeatures.size} found)</h2>")
        sb.append("<table><tr><th>Feature Name</th><th>Category</th><th>Status</th></tr>")
        for (pf in report.packageFeatures) {
            sb.append("<tr><td><code>${pf.name}</code></td><td>${pf.category}</td><td>${if (pf.isAvailable) "✅ Supported" else "❌ Not Advertised"}</td></tr>")
        }
        sb.append("</table>")

        sb.append("""
            <p style="margin-top: 40px; font-size: 12px; color: #8B949E; text-align: center;">
                Generated by Hardware Capability Analyzer Ultimate • Legally Accessible System Inspection
            </p>
            </body>
            </html>
        """.trimIndent())
        return sb.toString()
    }

    fun generateJsonReport(report: FullHardwareAnalysisReport): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(report.scanTimestamp))
        val hiddenList = report.hiddenCapabilityScores.joinToString(",\n") { s ->
            """
            {
              "capability": "${s.capabilityName.replace("\"", "'")}",
              "confidence_percent": ${s.confidencePercentage},
              "assessment": "${s.overallAssessment.replace("\"", "'")}",
              "android_restrictions": "${s.androidRestrictionsNote.replace("\"", "'")}"
            }
            """.trimIndent()
        }

        return """
        {
          "report_metadata": {
            "title": "Hardware Capability Analyzer Ultimate Report",
            "timestamp": "$dateStr",
            "device": "${report.deviceTitle}"
          },
          "system_info": {
            "android_version": "${report.systemInfo.androidVersion}",
            "api_level": ${report.systemInfo.apiLevel},
            "model": "${report.systemInfo.model}",
            "board": "${report.systemInfo.board}",
            "hardware": "${report.systemInfo.hardware}",
            "kernel": "${report.systemInfo.kernelVersion.replace("\"", "'")}",
            "cpu_arch": "${report.systemInfo.cpuArchitecture}",
            "cpu_cores": ${report.systemInfo.cpuCores}
          },
          "hidden_capability_scores": [
            $hiddenList
          ],
          "counts": {
            "package_features": ${report.packageFeatures.size},
            "system_properties": ${report.systemProperties.size},
            "libraries_scanned": ${report.scannedLibraries.size},
            "binder_services": ${report.binderServices.size},
            "codecs": ${report.codecs.size},
            "sensors": ${report.sensors.size},
            "cameras": ${report.cameras.size}
          }
        }
        """.trimIndent()
    }

    fun generateMarkdownReport(report: FullHardwareAnalysisReport): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(report.scanTimestamp))
        val sb = StringBuilder()
        sb.append("# ⚡ Hardware Capability Analyzer Ultimate Report\n\n")
        sb.append("- **Device:** ${report.deviceTitle} (${report.systemInfo.manufacturer} ${report.systemInfo.model})\n")
        sb.append("- **Android Version:** ${report.systemInfo.androidVersion} (API ${report.systemInfo.apiLevel})\n")
        sb.append("- **Board/Hardware:** ${report.systemInfo.board} / ${report.systemInfo.hardware}\n")
        sb.append("- **Scan Date:** $dateStr\n\n")

        sb.append("## 🔬 Hidden Capability & Risk Matrix\n\n")
        sb.append("| Capability | Confidence | Assessment |\n")
        sb.append("| :--- | :---: | :--- |\n")
        for (score in report.hiddenCapabilityScores) {
            sb.append("| **${score.capabilityName}** | `${score.confidencePercentage}%` | ${score.overallAssessment} |\n")
        }
        sb.append("\n")

        sb.append("## ⚙️ Qualcomm Components Detected\n\n")
        sb.append("| Component Name | Category | Location |\n")
        sb.append("| :--- | :--- | :--- |\n")
        for (qc in report.qualcommComponents) {
            sb.append("| `${qc.name}` | ${qc.category} | `${qc.detectedLocation}` |\n")
        }
        sb.append("\n")

        sb.append("## 📊 Summary Counts\n\n")
        sb.append("- **Package Features:** ${report.packageFeatures.size}\n")
        sb.append("- **System Properties Analyzed:** ${report.systemProperties.size}\n")
        sb.append("- **Libraries Scanned:** ${report.scannedLibraries.size}\n")
        sb.append("- **Binder Services Enumerated:** ${report.binderServices.size}\n")
        sb.append("- **Media Codecs:** ${report.codecs.size}\n")
        sb.append("- **Sensors Found:** ${report.sensors.size}\n")
        return sb.toString()
    }

    fun generateCsvReport(report: FullHardwareAnalysisReport): String {
        val sb = StringBuilder()
        sb.append("Category,Name,Status/Value,Details\n")
        for (score in report.hiddenCapabilityScores) {
            sb.append("HiddenCapability,${escapeCsv(score.capabilityName)},${score.confidencePercentage}%,${escapeCsv(score.overallAssessment)}\n")
        }
        for (qc in report.qualcommComponents) {
            sb.append("QualcommComponent,${escapeCsv(qc.name)},${escapeCsv(qc.category)},${escapeCsv(qc.detectedLocation)}\n")
        }
        for (pf in report.packageFeatures) {
            sb.append("PackageFeature,${escapeCsv(pf.name)},${if (pf.isAvailable) "Available" else "Not Available"},${escapeCsv(pf.category)}\n")
        }
        for (s in report.sensors) {
            sb.append("Sensor,${escapeCsv(s.name)},${escapeCsv(s.vendor)},Type ${s.type} MaxRange ${s.maxRange}\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(str: String): String {
        val replaced = str.replace("\"", "\"\"").replace("\n", " ")
        return "\"$replaced\""
    }

    fun exportAndShareFile(context: Context, fileName: String, content: String, mimeType: String) {
        try {
            val cacheDir = File(context.cacheDir, "reports")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val file = File(cacheDir, fileName)
            file.writeText(content)

            // Attempt sharing via standard Android Sharesheet
            val uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                null
            }

            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Hardware Capability Analysis Report: $fileName")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share Hardware Analysis Report")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
