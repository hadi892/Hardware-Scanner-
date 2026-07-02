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
import com.example.data.models.NetworkCapabilityModel
import com.example.data.models.SensorItem
import com.example.data.models.UsbCapabilityModel
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.components.SearchFilterBar
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80
import com.example.ui.viewmodel.FilterOption

@Composable
fun HwSensorsScreen(
    sensors: List<SensorItem>,
    usb: UsbCapabilityModel,
    network: NetworkCapabilityModel,
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    val filteredSensors = sensors.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, true) || it.vendor.contains(searchQuery, true)
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
                placeholderText = "Filter hardware sensors by name or vendor..."
            )
        }

        item {
            Text(
                text = "🔌 Module 12: USB Capability Analyzer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ExpandableInfoCard(
                title = "USB Host (OTG) & Peripheral Controller",
                subtitle = "Host/OTG Support: ${if (usb.hasUsbHostFeature) "✅ Enabled" else "❌ Disabled"} | Attached Devices: ${usb.attachedDevicesCount}",
                categoryBadge = "USB/OTG",
                initiallyExpanded = true,
                statusColor = if (usb.hasUsbHostFeature) TechEmerald80 else TechAmber80
            ) {
                Text("• USB Host Support (Required for RTL-SDR / USB tuners): ${usb.hasUsbHostFeature}", style = MaterialTheme.typography.bodySmall)
                Text("• USB Accessory Mode: ${usb.hasUsbAccessoryFeature}", style = MaterialTheme.typography.bodySmall)
                Text("• USB Audio Support: ${usb.supportsUsbAudio}", style = MaterialTheme.typography.bodySmall)
                Text("• USB Video Support (External Camera/UVC): ${usb.supportsUsbVideo}", style = MaterialTheme.typography.bodySmall)
                Text("• Attached Peripherals: ${usb.attachedDeviceNames.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🌐 Module 13: Network & Wireless Connectivity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ExpandableInfoCard(
                title = "Wireless Adapters & Network Interfaces",
                subtitle = "Active VPN: ${if (network.isVpnActive) "🛡️ Active" else "None"} | Ethernet Support: ${network.hasEthernetSupport}",
                categoryBadge = "Network",
                initiallyExpanded = true
            ) {
                Text("• WiFi Supported: ${network.hasWifi} | WiFi Direct: ${network.hasWifiDirect} | WiFi Aware: ${network.hasWifiAware}", style = MaterialTheme.typography.bodySmall)
                Text("• Bluetooth: ${network.hasBluetooth} | Bluetooth LE (BLE): ${network.hasBle}", style = MaterialTheme.typography.bodySmall)
                Text("• Ethernet Port / USB adapter support: ${network.hasEthernetSupport}", style = MaterialTheme.typography.bodySmall)
                Text("• Active Interfaces: ${network.activeInterfaceNames.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📡 Module 10: Hardware Sensor Analyzer (${filteredSensors.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredSensors) { sensor ->
            ExpandableInfoCard(
                title = sensor.name,
                subtitle = "Vendor: ${sensor.vendor} | Mode: ${sensor.reportingMode}",
                categoryBadge = "Type ${sensor.type}",
                statusColor = TechCyan80
            ) {
                Text("• Resolution: ${sensor.resolution}", style = MaterialTheme.typography.bodySmall)
                Text("• Maximum Range: ${sensor.maxRange}", style = MaterialTheme.typography.bodySmall)
                Text("• Power Draw: ${sensor.powerMa} mA", style = MaterialTheme.typography.bodySmall)
                Text("• Wake-up Capable: ${sensor.isWakeUpSensor} | FIFO Max Event Count: ${sensor.fifoMaxEventCount}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
