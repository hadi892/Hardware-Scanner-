package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.FullHardwareAnalysisReport
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechBlue80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80

@Composable
fun ReportGeneratorScreen(
    report: FullHardwareAnalysisReport,
    onExportReport: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TechCyan80)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📤 Module 17: Professional Report Generator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan80
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Compile and export complete hardware inspection audits. Generate standalone styled HTML pages, structured JSON data, Markdown audit sheets, or CSV tables and share directly via Android Sharesheet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "⚡ Select Export Format",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // HTML Card
                ExportCard(
                    title = "Interactive Styled HTML Report (.html)",
                    subtitle = "Complete responsive web document with CSS dark mode styling, summary tables, confidence progress bars, and device system breakdown.",
                    badge = "HTML5 Audit",
                    buttonText = "Generate & Share HTML",
                    color = TechCyan80,
                    icon = Icons.Default.Description,
                    onClick = { onExportReport("HTML") }
                )

                // JSON Card
                ExportCard(
                    title = "Structured Machine JSON (.json)",
                    subtitle = "Parsable JSON payload containing full system specifications, calculated confidence metrics, and module item counts suitable for automated pipeline ingests.",
                    badge = "REST / JSON",
                    buttonText = "Generate & Share JSON",
                    color = TechEmerald80,
                    icon = Icons.Default.Code,
                    onClick = { onExportReport("JSON") }
                )

                // Markdown Card
                ExportCard(
                    title = "Formatted Markdown Sheet (.md)",
                    subtitle = "Clean GitHub-flavored markdown tables summarizing hidden capability probabilities, Qualcomm components, and system architecture.",
                    badge = "Markdown Table",
                    buttonText = "Generate & Share MD",
                    color = TechBlue80,
                    icon = Icons.Default.Description,
                    onClick = { onExportReport("MD") }
                )

                // CSV Card
                ExportCard(
                    title = "Tabular Data Spreadsheet (.csv)",
                    subtitle = "Flattened CSV export of every enumerated feature, property, library, sensor, and capability score for Excel / Google Sheets analysis.",
                    badge = "CSV Table",
                    buttonText = "Generate & Share CSV",
                    color = TechAmber80,
                    icon = Icons.Default.TableChart,
                    onClick = { onExportReport("CSV") }
                )
            }
        }
    }
}

@Composable
private fun ExportCard(
    title: String,
    subtitle: String,
    badge: String,
    buttonText: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = color)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = badge, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.2f), contentColor = color)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}
