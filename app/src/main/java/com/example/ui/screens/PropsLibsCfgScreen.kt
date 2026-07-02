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
import com.example.data.models.ConfigFileItem
import com.example.data.models.LibraryItem
import com.example.data.models.SystemPropertyItem
import com.example.ui.components.CodeSnippetBox
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.components.SearchFilterBar
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.viewmodel.FilterOption

@Composable
fun PropsLibsCfgScreen(
    systemProperties: List<SystemPropertyItem>,
    scannedLibraries: List<LibraryItem>,
    configFiles: List<ConfigFileItem>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterChanged: (FilterOption) -> Unit
) {
    val filteredProps = systemProperties.filter {
        val matchesQ = searchQuery.isEmpty() || it.key.contains(searchQuery, true) || it.value.contains(searchQuery, true)
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.HIGHLIGHTED_ONLY -> it.isHighlighted
            else -> true
        }
        matchesQ && matchesF
    }

    val filteredLibs = scannedLibraries.filter {
        val matchesQ = searchQuery.isEmpty() || it.fileName.contains(searchQuery, true) || it.path.contains(searchQuery, true)
        val matchesF = when (selectedFilter) {
            FilterOption.ALL -> true
            FilterOption.CONFIRMED_ONLY -> it.existsOnDisk
            FilterOption.HIGHLIGHTED_ONLY -> it.category.contains("FM") || it.category.contains("Qualcomm") || it.category.contains("Tuner")
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
                placeholderText = "Search properties, libraries (*fm*, *qcom*)..."
            )
        }

        item {
            Text(
                text = "📜 Module 3: System Property Scanner (${filteredProps.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredProps.take(80)) { prop ->
            ExpandableInfoCard(
                title = prop.key,
                subtitle = "Value: ${prop.value}",
                categoryBadge = prop.category,
                isHighlighted = prop.isHighlighted,
                statusColor = if (prop.isHighlighted) TechAmber80 else TechCyan80
            ) {
                Text("• Full Key: ${prop.key}", style = MaterialTheme.typography.bodySmall)
                Text("• Full Value: ${prop.value}", style = MaterialTheme.typography.bodySmall)
                Text("• Matched keyword: ${prop.isHighlighted}", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "📚 Module 4: Native Library Scanner (${filteredLibs.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(filteredLibs.take(60)) { lib ->
            ExpandableInfoCard(
                title = lib.fileName,
                subtitle = lib.path,
                categoryBadge = lib.category,
                isHighlighted = lib.category.contains("FM") || lib.category.contains("Tuner"),
                statusColor = if (lib.existsOnDisk) TechCyan80 else TechAmber80
            ) {
                Text("• Path: ${lib.path}", style = MaterialTheme.typography.bodySmall)
                Text("• Exists on Disk: ${lib.existsOnDisk} (${lib.sizeBytes} bytes)", style = MaterialTheme.typography.bodySmall)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "🛠️ Module 5: Configuration XML Scanner (${configFiles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(configFiles) { cfg ->
            ExpandableInfoCard(
                title = cfg.path,
                subtitle = "Keywords: ${cfg.keywordMatched}",
                categoryBadge = if (cfg.isReadable) "Readable" else "Protected",
                isHighlighted = cfg.keywordMatched.contains("FM") || cfg.keywordMatched.contains("Tuner")
            ) {
                Text("• Target Path: ${cfg.path}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(6.dp))
                CodeSnippetBox(code = cfg.previewExcerpt)
            }
        }
    }
}
