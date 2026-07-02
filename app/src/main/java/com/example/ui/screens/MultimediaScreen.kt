package com.example.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.AudioCapabilityModel
import com.example.data.models.CameraItem
import com.example.data.models.CodecItem
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.components.SearchFilterBar
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80
import com.example.ui.viewmodel.FilterOption

@Composable
fun MultimediaScreen(
    codecs: List<CodecItem>,
    audio: AudioCapabilityModel,
    cameras: List<CameraItem>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterChanged: (FilterOption) -> Unit
) {
    val filteredCodecs = codecs.filter {
        val matchesQ = searchQuery.isEmpty() || it.name.contains(searchQuery, true) || it.supportedTypes.any { t -> t.contains(searchQuery, true) }
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.CONFIRMED_ONLY -> it.isHardwareAccelerated
            FilterOption.HIGHLIGHTED_ONLY -> it.isVendorSpecific
            else -> true
        }
        matchesQ && matchesF
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SearchFilterBar(
                searchQuery = searchQuery,
                onSearchChanged = onSearchChanged,
                selectedFilter = selectedFilter,
                onFilterChanged = onFilterChanged,
                placeholderText = "Filter codecs, cameras, or audio endpoints..."
            )
        }

        item {
            Text(
                text = "🔊 Module 9: Audio Subsystem & FM Routing Scanner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ExpandableInfoCard(
                title = "Audio Manager Routing Inspection",
                subtitle = if (audio.detectedFmRoutingIndication) "⚠️ FM Audio Routing Endpoints Discovered" else "Standard Audio Endpoints Only",
                categoryBadge = "Audio/FM",
                isHighlighted = audio.detectedFmRoutingIndication,
                statusColor = if (audio.detectedFmRoutingIndication) TechAmber80 else TechCyan80,
                initiallyExpanded = true
            ) {
                Text("• Audio Mode: ${audio.mode}", style = MaterialTheme.typography.bodySmall)
                Text("• Output Devices (${audio.outputDevices.size}): ${audio.outputDevices.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Input Devices (${audio.inputDevices.size}): ${audio.inputDevices.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Supported Sample Rates: ${audio.sampleRatesSupported.joinToString(", ")} Hz", style = MaterialTheme.typography.bodySmall)
                Text("• FM Routing Evidence: ${audio.fmRoutingEvidenceDetails}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📸 Module 11: Camera Capability Analyzer (${cameras.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (cameras.isEmpty()) {
            item {
                ExpandableInfoCard(
                    title = "Camera Enumeration Notice",
                    subtitle = "Camera IDs restricted or camera permission not yet granted.",
                    categoryBadge = "Notice"
                ) {
                    Text("Grant camera permission to view detailed sensor resolution and logical multi-camera breakdown.", style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            items(cameras) { cam ->
                ExpandableInfoCard(
                    title = "Camera ID ${cam.cameraId} (${cam.facing} Facing)",
                    subtitle = "Resolution: ${cam.resolutionsMegapixels} | Level: ${cam.hardwareLevel}",
                    categoryBadge = if (cam.isLogicalMultiCamera) "Logical Multi" else "Physical Sensor",
                    statusColor = TechEmerald80
                ) {
                    Text("• Hardware Level: ${cam.hardwareLevel}", style = MaterialTheme.typography.bodySmall)
                    if (cam.physicalCameraIds.isNotEmpty()) {
                        Text("• Backing Physical IDs: ${cam.physicalCameraIds.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("• Vendor Extensions: ${cam.vendorExtensionsSupported.joinToString("; ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🎞️ Module 8: MediaCodec Analyzer (${filteredCodecs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredCodecs) { codec ->
            ExpandableInfoCard(
                title = codec.name,
                subtitle = "${if (codec.isEncoder) "Encoder" else "Decoder"} | Types: ${codec.supportedTypes.take(2).joinToString(", ")}",
                categoryBadge = if (codec.isHardwareAccelerated) "HW Accelerated" else "Software",
                isHighlighted = codec.isVendorSpecific,
                statusColor = if (codec.isHardwareAccelerated) TechEmerald80 else TechCyan80
            ) {
                Text("• Supported MIME Types: ${codec.supportedTypes.joinToString("\n  ")}", style = MaterialTheme.typography.bodySmall)
                Text("• Is Hardware Accelerated: ${codec.isHardwareAccelerated}", style = MaterialTheme.typography.bodySmall)
                Text("• Vendor Codec: ${codec.isVendorSpecific}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
