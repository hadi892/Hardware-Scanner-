package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ScanReportEntity
import com.example.data.models.FullHardwareAnalysisReport
import com.example.ui.components.ConfidenceProgressMeter
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechBlue80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80
import com.example.ui.theme.TechRed80
import com.example.ui.viewmodel.ModuleTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    report: FullHardwareAnalysisReport,
    isLoading: Boolean,
    onRunScan: () -> Unit,
    onNavigateToTab: (ModuleTab) -> Unit,
    scanHistory: List<ScanReportEntity>,
    onClearHistory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Device Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.35f), Color(0xFF0E7490).copy(alpha = 0.35f))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Target Hardware Inspector",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TechCyan80,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${report.systemInfo.manufacturer} ${report.systemInfo.model}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Board: ${report.systemInfo.board} | Android ${report.systemInfo.androidVersion} (API ${report.systemInfo.apiLevel})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onRunScan,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Scan", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Re-Scan")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = TechEmerald80, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Root Free • Bootloader Lock Safe • Android 14/15/16 Compliant Inspector",
                                style = MaterialTheme.typography.bodySmall,
                                color = TechEmerald80,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Hidden Capability Confidence Breakdown (Module 15)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = TechCyan80)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🔬 Module 15: Hidden Capability Confidence Dial",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        OutlinedButton(
                            onClick = { onNavigateToTab(ModuleTab.MODULE_HIDDEN_RISK) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Risk Details", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    report.hiddenCapabilityScores.forEach { score ->
                        ConfidenceProgressMeter(score = score.confidencePercentage, label = score.capabilityName)
                    }
                }
            }
        }

        // 3. Module Summary Grid (Counts & Status)
        item {
            Text(
                text = "📦 Comprehensive Scanner Module Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            val stats = listOf(
                Triple("Module 1: System Info", "${report.systemInfo.cpuCores} Cores / ${report.systemInfo.cpuArchitecture}", ModuleTab.MODULE_SYSINFO),
                Triple("Module 2: Packages", "${report.packageFeatures.size} Features Enumerated", ModuleTab.MODULE_PACKAGES_HAL),
                Triple("Module 3: Properties", "${report.systemProperties.size} Properties Analyzed", ModuleTab.MODULE_PROPS_LIBS_CFG),
                Triple("Module 4: Libraries", "${report.scannedLibraries.size} Targeted SO Files Found", ModuleTab.MODULE_PROPS_LIBS_CFG),
                Triple("Module 6 & 7: HAL / Binder", "${report.binderServices.size} Services / ${report.halCapabilities.size} HALs", ModuleTab.MODULE_PACKAGES_HAL),
                Triple("Module 8: Media Codecs", "${report.codecs.size} Hardware & Software Codecs", ModuleTab.MODULE_MULTIMEDIA),
                Triple("Module 10: Hardware Sensors", "${report.sensors.size} Physical Sensors Listed", ModuleTab.MODULE_HW_SENSORS),
                Triple("Module 14: Qualcomm SoC", "${report.qualcommComponents.size} Snapdragon DSP/RF Components", ModuleTab.MODULE_SYSINFO)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stats.chunked(2).forEach { rowPair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowPair.forEach { (title, sub, tab) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTab(tab) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TechCyan80)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = sub, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (rowPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // 4. Scan History (Room DB)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗄️ Room DB Scan History Log (${scanHistory.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (scanHistory.isNotEmpty()) {
                    IconButton(onClick = onClearHistory) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = TechRed80)
                    }
                }
            }
        }

        if (scanHistory.isEmpty()) {
            item {
                Text(
                    text = "No prior background scans recorded in local Room DB yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(scanHistory.take(5)) { entity ->
                val dt = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US).format(Date(entity.timestampMs))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${entity.deviceName} [Android ${entity.androidVersion}]", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "$dt • ${entity.totalFindingsCount} items recorded", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TechCyan80.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "${entity.highestHiddenConfidenceScore}% Max Risk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TechCyan80)
                        }
                    }
                }
            }
        }
    }
}
