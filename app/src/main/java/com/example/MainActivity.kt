package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HiddenRiskScreen
import com.example.ui.screens.HwSensorsScreen
import com.example.ui.screens.MultimediaScreen
import com.example.ui.screens.PackagesHalScreen
import com.example.ui.screens.PropsLibsCfgScreen
import com.example.ui.screens.ReportGeneratorScreen
import com.example.ui.screens.SystemInfoScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TechCyan80
import com.example.ui.viewmodel.HardwareViewModel
import com.example.ui.viewmodel.ModuleTab

class MainActivity : ComponentActivity() {

    private val viewModel: HardwareViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val report by viewModel.report.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()
            val selectedTab by viewModel.selectedTab.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val selectedFilter by viewModel.selectedFilter.collectAsState()
            val scanHistory by viewModel.scanHistory.collectAsState()

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                viewModel.runFullScan()
            }

            LaunchedEffect(Unit) {
                val perms = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    perms.add(Manifest.permission.BLUETOOTH_CONNECT)
                }
                val ungranted = perms.filter {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) != PackageManager.PERMISSION_GRANTED
                }
                if (ungranted.isNotEmpty()) {
                    permissionLauncher.launch(ungranted.toTypedArray())
                }
            }

            val configuration = LocalConfiguration.current
            val isWideScreen = configuration.screenWidthDp >= 600

            MyApplicationTheme(darkTheme = isDarkMode) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚡ Hardware Capability Analyzer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "[Ultimate]",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                    Icon(
                                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ModuleTab.values().forEach { tab ->
                                            FilterChip(
                                                selected = selectedTab == tab,
                                                onClick = { viewModel.selectTab(tab) },
                                                label = { Text(tab.title, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (isLoading && report == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Auditing hardware capabilities, HAL & SoC features...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else if (report != null) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (isWideScreen) {
                                    NavigationRail(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        header = {
                                            Text("Modules", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                                        }
                                    ) {
                                        ModuleTab.values().forEach { tab ->
                                            NavigationRailItem(
                                                selected = selectedTab == tab,
                                                onClick = { viewModel.selectTab(tab) },
                                                icon = { Icon(getTabIcon(tab), contentDescription = tab.title) },
                                                label = { Text(tab.title, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    when (selectedTab) {
                                        ModuleTab.DASHBOARD -> DashboardScreen(
                                            report = report!!,
                                            isLoading = isLoading,
                                            onRunScan = { viewModel.runFullScan() },
                                            onNavigateToTab = { viewModel.selectTab(it) },
                                            scanHistory = scanHistory,
                                            onClearHistory = { viewModel.clearScanHistory() }
                                        )
                                        ModuleTab.MODULE_SYSINFO -> SystemInfoScreen(
                                            systemInfo = report!!.systemInfo,
                                            qualcommComponents = report!!.qualcommComponents,
                                            searchQuery = searchQuery,
                                            onSearchChanged = { viewModel.updateSearchQuery(it) }
                                        )
                                        ModuleTab.MODULE_PACKAGES_HAL -> PackagesHalScreen(
                                            packageFeatures = report!!.packageFeatures,
                                            binderServices = report!!.binderServices,
                                            halCapabilities = report!!.halCapabilities,
                                            searchQuery = searchQuery,
                                            onSearchChanged = { viewModel.updateSearchQuery(it) },
                                            selectedFilter = selectedFilter,
                                            onFilterChanged = { viewModel.updateFilter(it) }
                                        )
                                        ModuleTab.MODULE_PROPS_LIBS_CFG -> PropsLibsCfgScreen(
                                            systemProperties = report!!.systemProperties,
                                            scannedLibraries = report!!.scannedLibraries,
                                            configFiles = report!!.configurationFiles,
                                            searchQuery = searchQuery,
                                            onSearchChanged = { viewModel.updateSearchQuery(it) },
                                            selectedFilter = selectedFilter,
                                            onFilterChanged = { viewModel.updateFilter(it) }
                                        )
                                        ModuleTab.MODULE_MULTIMEDIA -> MultimediaScreen(
                                            codecs = report!!.codecs,
                                            audio = report!!.audioCapability,
                                            cameras = report!!.cameras,
                                            searchQuery = searchQuery,
                                            onSearchChanged = { viewModel.updateSearchQuery(it) },
                                            selectedFilter = selectedFilter,
                                            onFilterChanged = { viewModel.updateFilter(it) }
                                        )
                                        ModuleTab.MODULE_HW_SENSORS -> HwSensorsScreen(
                                            sensors = report!!.sensors,
                                            usb = report!!.usbCapability,
                                            network = report!!.networkCapability,
                                            searchQuery = searchQuery,
                                            onSearchChanged = { viewModel.updateSearchQuery(it) }
                                        )
                                        ModuleTab.MODULE_HIDDEN_RISK -> HiddenRiskScreen(
                                            scores = report!!.hiddenCapabilityScores
                                        )
                                        ModuleTab.MODULE_REPORTS -> ReportGeneratorScreen(
                                            report = report!!,
                                            onExportReport = { viewModel.exportReport(it) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getTabIcon(tab: ModuleTab): ImageVector {
        return when (tab) {
            ModuleTab.DASHBOARD -> Icons.Default.Dashboard
            ModuleTab.MODULE_SYSINFO -> Icons.Default.Memory
            ModuleTab.MODULE_PACKAGES_HAL -> Icons.Default.Extension
            ModuleTab.MODULE_PROPS_LIBS_CFG -> Icons.Default.Folder
            ModuleTab.MODULE_MULTIMEDIA -> Icons.Default.Speaker
            ModuleTab.MODULE_HW_SENSORS -> Icons.Default.Sensors
            ModuleTab.MODULE_HIDDEN_RISK -> Icons.Default.Security
            ModuleTab.MODULE_REPORTS -> Icons.Default.Assessment
        }
    }
}
