package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val deviceName: String,
    val androidVersion: String,
    val highestHiddenConfidenceScore: Int,
    val highestRiskName: String,
    val totalFindingsCount: Int,
    val reportJsonSnapshot: String
)
