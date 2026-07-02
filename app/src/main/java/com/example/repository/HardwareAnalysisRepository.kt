package com.example.repository

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.db.ScanReportDao
import com.example.data.db.ScanReportEntity
import com.example.data.models.FullHardwareAnalysisReport
import com.example.data.scanners.AudioScanner
import com.example.data.scanners.BinderScanner
import com.example.data.scanners.CameraScanner
import com.example.data.scanners.CodecScanner
import com.example.data.scanners.ConfigScanner
import com.example.data.scanners.HalScanner
import com.example.data.scanners.HardwareScanWorker
import com.example.data.scanners.HiddenCapabilityDetector
import com.example.data.scanners.LibraryScanner
import com.example.data.scanners.NetworkScanner
import com.example.data.scanners.PackageManagerScanner
import com.example.data.scanners.QualcommScanner
import com.example.data.scanners.ReportGenerator
import com.example.data.scanners.SensorScanner
import com.example.data.scanners.SystemInfoScanner
import com.example.data.scanners.SystemPropertyScanner
import com.example.data.scanners.UsbScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HardwareAnalysisRepository(
    private val context: Context,
    private val scanReportDao: ScanReportDao
) {

    val scanHistory: Flow<List<ScanReportEntity>> = scanReportDao.getAllReports()

    suspend fun performComprehensiveScan(): FullHardwareAnalysisReport = withContext(Dispatchers.IO) {
        val sysInfo = SystemInfoScanner.scan(context)
        val pkgs = PackageManagerScanner.scan(context)
        val props = SystemPropertyScanner.scan()
        val libs = LibraryScanner.scan()
        val configs = ConfigScanner.scan()
        val binders = BinderScanner.scan()
        val hals = HalScanner.scan(context)
        val codecs = CodecScanner.scan()
        val audio = AudioScanner.scan(context)
        val sensors = SensorScanner.scan(context)
        val cameras = CameraScanner.scan(context)
        val usb = UsbScanner.scan(context)
        val net = NetworkScanner.scan(context)
        val qcom = QualcommScanner.scan(libs, props)
        val hidden = HiddenCapabilityDetector.analyze(pkgs, props, libs, configs, binders, hals, audio, qcom)

        val report = FullHardwareAnalysisReport(
            scanTimestamp = System.currentTimeMillis(),
            deviceTitle = "${sysInfo.manufacturer} ${sysInfo.model}",
            systemInfo = sysInfo,
            packageFeatures = pkgs,
            systemProperties = props,
            scannedLibraries = libs,
            configurationFiles = configs,
            binderServices = binders,
            halCapabilities = hals,
            codecs = codecs,
            audioCapability = audio,
            sensors = sensors,
            cameras = cameras,
            usbCapability = usb,
            networkCapability = net,
            qualcommComponents = qcom,
            hiddenCapabilityScores = hidden
        )

        val maxHidden = hidden.maxByOrNull { it.confidencePercentage }
        val highestScore = maxHidden?.confidencePercentage ?: 0
        val highestName = maxHidden?.capabilityName ?: "Standard"
        val totalFindings = pkgs.size + props.size + libs.size + binders.size + codecs.size + sensors.size + cameras.size

        scanReportDao.insertReport(
            ScanReportEntity(
                timestampMs = report.scanTimestamp,
                deviceName = report.deviceTitle,
                androidVersion = sysInfo.androidVersion,
                highestHiddenConfidenceScore = highestScore,
                highestRiskName = highestName,
                totalFindingsCount = totalFindings,
                reportJsonSnapshot = "Complete Hardware Analysis snapshot recorded."
            )
        )

        report
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        scanReportDao.deleteAllReports()
    }

    fun schedulePeriodicBackgroundScan() {
        val workReq = PeriodicWorkRequestBuilder<HardwareScanWorker>(12, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "hardware_periodic_monitor",
            ExistingPeriodicWorkPolicy.KEEP,
            workReq
        )
    }

    fun exportAndShareReport(report: FullHardwareAnalysisReport, format: String) {
        val timestampStr = report.scanTimestamp.toString()
        when (format.uppercase()) {
            "HTML" -> {
                val content = ReportGenerator.generateHtmlReport(report)
                ReportGenerator.exportAndShareFile(context, "Hardware_Analysis_$timestampStr.html", content, "text/html")
            }
            "JSON" -> {
                val content = ReportGenerator.generateJsonReport(report)
                ReportGenerator.exportAndShareFile(context, "Hardware_Analysis_$timestampStr.json", content, "application/json")
            }
            "MD", "MARKDOWN" -> {
                val content = ReportGenerator.generateMarkdownReport(report)
                ReportGenerator.exportAndShareFile(context, "Hardware_Analysis_$timestampStr.md", content, "text/markdown")
            }
            "CSV" -> {
                val content = ReportGenerator.generateCsvReport(report)
                ReportGenerator.exportAndShareFile(context, "Hardware_Analysis_$timestampStr.csv", content, "text/csv")
            }
        }
    }
}
