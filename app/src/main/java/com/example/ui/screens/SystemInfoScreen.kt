package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.SystemInfoModel
import com.example.data.models.QualcommComponentModel
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.components.SearchFilterBar
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.viewmodel.FilterOption

@Composable
fun SystemInfoScreen(
    systemInfo: SystemInfoModel,
    qualcommComponents: List<QualcommComponentModel>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    val qcomFiltered = qualcommComponents.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, true) ||
                it.category.contains(searchQuery, true) || it.detectedLocation.contains(searchQuery, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SearchFilterBar(
                searchQuery = searchQuery,
                onSearchChanged = onSearchChanged,
                selectedFilter = FilterOption.ALL,
                onFilterChanged = {},
                placeholderText = "Filter hardware specs or Qualcomm components..."
            )
        }

        item {
            Text(
                text = "🖥️ Module 1: Comprehensive System Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ExpandableInfoCard(
                title = "Build & OS Specifications",
                subtitle = "Android ${systemInfo.androidVersion} (API ${systemInfo.apiLevel}) | Security Patch: ${systemInfo.securityPatch}",
                categoryBadge = "System OS",
                initiallyExpanded = true
            ) {
                Text("• Manufacturer: ${systemInfo.manufacturer}", style = MaterialTheme.typography.bodySmall)
                Text("• Brand / Model: ${systemInfo.brand} (${systemInfo.model})", style = MaterialTheme.typography.bodySmall)
                Text("• Product / Device: ${systemInfo.product} / ${systemInfo.device}", style = MaterialTheme.typography.bodySmall)
                Text("• Board / Hardware: ${systemInfo.board} / ${systemInfo.hardware}", style = MaterialTheme.typography.bodySmall)
                Text("• Build Type / Tags: ${systemInfo.buildType} (${systemInfo.buildTags})", style = MaterialTheme.typography.bodySmall)
                Text("• Fingerprint: ${systemInfo.buildFingerprint}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            ExpandableInfoCard(
                title = "Processor & Kernel Architecture",
                subtitle = "${systemInfo.cpuArchitecture} (${systemInfo.cpuCores} Cores Available)",
                categoryBadge = "CPU / Kernel",
                initiallyExpanded = true
            ) {
                Text("• Supported ABIs: ${systemInfo.supportedAbis.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Linux Kernel Version: ${systemInfo.kernelVersion}", style = MaterialTheme.typography.bodySmall)
                Text("• Bootloader Version: ${systemInfo.bootloader}", style = MaterialTheme.typography.bodySmall)
                Text("• Baseband / Radio: ${systemInfo.radioVersion}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            ExpandableInfoCard(
                title = "Memory (RAM) & Internal Storage",
                subtitle = "RAM Available: %.2f GB / %.2f GB | Storage Available: %.2f GB / %.2f GB".format(
                    systemInfo.availableMemoryBytes / 1e9, systemInfo.totalMemoryBytes / 1e9,
                    systemInfo.availableStorageBytes / 1e9, systemInfo.totalStorageBytes / 1e9
                ),
                categoryBadge = "Memory/Disk"
            ) {
                Text("• RAM Bytes: ${systemInfo.availableMemoryBytes} avail of ${systemInfo.totalMemoryBytes}", style = MaterialTheme.typography.bodySmall)
                Text("• Storage Bytes: ${systemInfo.availableStorageBytes} avail of ${systemInfo.totalStorageBytes}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            ExpandableInfoCard(
                title = "Display & GPU Surface Metrics",
                subtitle = "${systemInfo.screenResolution} | ${systemInfo.gpuRenderer}",
                categoryBadge = "Display/GPU"
            ) {
                Text("• Display Metrics: ${systemInfo.displayMetricsInfo}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "⚙️ Module 14: Qualcomm Component Scanner (${qcomFiltered.size} items)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(qcomFiltered) { qc ->
            ExpandableInfoCard(
                title = qc.name,
                subtitle = "Category: ${qc.category}",
                categoryBadge = "Qualcomm",
                isHighlighted = qc.category.contains("DSP") || qc.category.contains("RF"),
                statusColor = if (qc.category.contains("DSP")) TechAmber80 else TechCyan80
            ) {
                Text("• Detected Location: ${qc.detectedLocation}", style = MaterialTheme.typography.bodySmall)
                Text("• Confidence Assessment: ${qc.confidence}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
