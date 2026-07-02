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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.models.BinderServiceItem
import com.example.data.models.HalCapabilityItem
import com.example.data.models.PackageFeatureModel
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.components.SearchFilterBar
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80
import com.example.ui.theme.TechRed80
import com.example.ui.viewmodel.FilterOption

@Composable
fun PackagesHalScreen(
    packageFeatures: List<PackageFeatureModel>,
    binderServices: List<BinderServiceItem>,
    halCapabilities: List<HalCapabilityItem>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterChanged: (FilterOption) -> Unit
) {
    val filteredHals = halCapabilities.filter {
        val matchesQ = searchQuery.isEmpty() || it.halName.contains(searchQuery, true) || it.category.contains(searchQuery, true)
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.CONFIRMED_ONLY -> it.status.name == "CONFIRMED"
            FilterOption.HIGHLIGHTED_ONLY -> it.category.contains("Radio") || it.category.contains("Tuner")
            FilterOption.RESTRICTED_ONLY -> it.status.name != "CONFIRMED"
        }
        matchesQ && matchesF
    }

    val filteredBinders = binderServices.filter {
        val matchesQ = searchQuery.isEmpty() || it.serviceName.contains(searchQuery, true) || it.purposeGuess.contains(searchQuery, true)
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.CONFIRMED_ONLY -> it.isAccessible
            FilterOption.HIGHLIGHTED_ONLY -> it.isSuspiciousMultimedia
            FilterOption.RESTRICTED_ONLY -> !it.isAccessible
        }
        matchesQ && matchesF
    }

    val filteredPkgs = packageFeatures.filter {
        val matchesQ = searchQuery.isEmpty() || it.name.contains(searchQuery, true) || it.category.contains(searchQuery, true)
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.CONFIRMED_ONLY -> it.isAvailable
            FilterOption.HIGHLIGHTED_ONLY -> it.category.contains("FM") || it.category.contains("Audio")
            FilterOption.RESTRICTED_ONLY -> !it.isAvailable
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
                placeholderText = "Filter packages, HALs, or binder services..."
            )
        }

        item {
            Text(
                text = "🛡️ Module 7: HAL Capability Scanner (${filteredHals.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredHals) { hal ->
            ExpandableInfoCard(
                title = hal.halName,
                subtitle = "Category: ${hal.category} | Status: ${hal.status}",
                categoryBadge = "HAL",
                isHighlighted = hal.category.contains("Radio") || hal.category.contains("Tuner"),
                statusColor = if (hal.status.name == "CONFIRMED") TechEmerald80 else TechAmber80
            ) {
                Text("• Info: ${hal.versionOrInfo}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🔗 Module 6: Binder Service Enumerator (${filteredBinders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredBinders.take(60)) { binder ->
            ExpandableInfoCard(
                title = binder.serviceName,
                subtitle = binder.purposeGuess,
                categoryBadge = if (binder.isAccessible) "Active" else "Restricted",
                isHighlighted = binder.isSuspiciousMultimedia,
                statusColor = if (binder.isAccessible) TechCyan80 else TechRed80
            ) {
                Text("• Interface Descriptor: ${binder.interfaceDescriptor}", style = MaterialTheme.typography.bodySmall)
                Text("• Accessible directly from app sandbox: ${binder.isAccessible}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "📦 Module 2: PackageManager System Features (${filteredPkgs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredPkgs.take(100)) { pkg ->
            ExpandableInfoCard(
                title = pkg.name,
                subtitle = if (pkg.isAvailable) "Supported (Version: ${pkg.version})" else "Feature Flag Not Advertised",
                categoryBadge = pkg.category,
                isHighlighted = pkg.category.contains("FM"),
                statusColor = if (pkg.isAvailable) TechEmerald80 else Color.Gray
            ) {
                Text("• Category: ${pkg.category}", style = MaterialTheme.typography.bodySmall)
                Text("• PackageManager reported hasSystemFeature = ${pkg.isAvailable}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
