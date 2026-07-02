package com.example.data.scanners

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.db.ScanReportEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HardwareScanWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sysInfo = SystemInfoScanner.scan(applicationContext)
            val pkgs = PackageManagerScanner.scan(applicationContext)
            val props = SystemPropertyScanner.scan()
            val libs = LibraryScanner.scan()
            val configs = ConfigScanner.scan()
            val binders = BinderScanner.scan()
            val hals = HalScanner.scan(applicationContext)
            val codecs = CodecScanner.scan()
            val audio = AudioScanner.scan(applicationContext)
            val sensors = SensorScanner.scan(applicationContext)
            val cameras = CameraScanner.scan(applicationContext)
            val usb = UsbScanner.scan(applicationContext)
            val net = NetworkScanner.scan(applicationContext)
            val qcom = QualcommScanner.scan(libs, props)
            val hidden = HiddenCapabilityDetector.analyze(pkgs, props, libs, configs, binders, hals, audio, qcom)

            val maxHidden = hidden.maxByOrNull { it.confidencePercentage }
            val highestScore = maxHidden?.confidencePercentage ?: 0
            val highestName = maxHidden?.capabilityName ?: "None"
            val totalFindings = pkgs.size + props.size + libs.size + binders.size + codecs.size + sensors.size + cameras.size

            val db = AppDatabase.getDatabase(applicationContext)
            db.scanReportDao().insertReport(
                ScanReportEntity(
                    timestampMs = System.currentTimeMillis(),
                    deviceName = "${sysInfo.manufacturer} ${sysInfo.model} (${sysInfo.board})",
                    androidVersion = sysInfo.androidVersion,
                    highestHiddenConfidenceScore = highestScore,
                    highestRiskName = highestName,
                    totalFindingsCount = totalFindings,
                    reportJsonSnapshot = "Background scheduled inspection completed. Found $totalFindings items."
                )
            )
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
