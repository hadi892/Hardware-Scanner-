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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.HiddenCapabilityScore
import com.example.ui.components.ConfidenceProgressMeter
import com.example.ui.components.ExpandableInfoCard
import com.example.ui.theme.TechAmber80
import com.example.ui.theme.TechCyan80
import com.example.ui.theme.TechEmerald80
import com.example.ui.theme.TechRed80

@Composable
fun HiddenRiskScreen(
    scores: List<HiddenCapabilityScore>
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
                        Icon(Icons.Default.Policy, contentDescription = null, tint = TechCyan80)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔬 Module 15 & 16: Hidden Hardware & Risk Analysis",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechCyan80
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calculates empirical probability scores (0–100%) based purely on genuine evidence found across PackageManager, HAL, Binder IPC, and native library structures without root or custom ROMs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Explanation of Android Security Restrictions (Module 16)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TechAmber80)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "🛡️ Android 14, 15 & 16 Architectural Restrictions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TechAmber80)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Treble / HIDL / AIDL Isolation: Direct kernel character device nodes (/dev/radio0, /dev/snd/*) are blocked from unprivileged app sandboxes by strict SELinux MAC policy.\n" +
                                "• FM Radio & Digital TV Tuners: Even when Qualcomm SoC hardware features an embedded FM receiver (WCN685x/FastConnect), public APIs were deprecated in Android 10+. Accessing the physical tuner requires either OEM system signature permissions or USB Host OTG dongles (RTL-SDR).\n" +
                                "• Zero Hallucination Guarantee: Scores reflect verified system evidence without inferring unsupported hardware as fact.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        item {
            Text(
                text = "⚡ Evaluated Hidden Capabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(scores) { score ->
            val color = when {
                score.confidencePercentage >= 65 -> TechEmerald80
                score.confidencePercentage >= 35 -> TechAmber80
                else -> TechRed80
            }

            ExpandableInfoCard(
                title = score.capabilityName,
                subtitle = "${score.overallAssessment}",
                categoryBadge = "${score.confidencePercentage}% Confidence",
                isHighlighted = score.confidencePercentage >= 35,
                statusColor = color,
                initiallyExpanded = true
            ) {
                ConfidenceProgressMeter(score = score.confidencePercentage, label = "Probability Score")
                Spacer(modifier = Modifier.height(8.dp))

                Text("✅ Evidence Found (${score.evidenceFound.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TechEmerald80)
                score.evidenceFound.forEach { ev ->
                    Text("  • $ev", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("❌ Evidence Missing (${score.evidenceMissing.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TechRed80)
                score.evidenceMissing.forEach { mis ->
                    Text("  • $mis", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("⚠️ Android Sandboxing Note:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TechAmber80)
                Text(score.androidRestrictionsNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
