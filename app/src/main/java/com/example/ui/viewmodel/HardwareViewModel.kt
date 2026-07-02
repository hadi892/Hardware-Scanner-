package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.ScanReportEntity
import com.example.data.models.FullHardwareAnalysisReport
import com.example.di.DIContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ModuleTab(val title: String, val iconName: String) {
    DASHBOARD("Dashboard & Score", "dashboard"),
    MODULE_SYSINFO("System & Qualcomm", "memory"),
    MODULE_PACKAGES_HAL("Packages & HAL", "extension"),
    MODULE_PROPS_LIBS_CFG("Properties & Libs", "folder"),
    MODULE_MULTIMEDIA("Audio, Codecs & Cam", "speaker"),
    MODULE_HW_SENSORS("Sensors, USB & Net", "sensors"),
    MODULE_HIDDEN_RISK("Hidden Risk Matrix", "security"),
    MODULE_REPORTS("Report Generator", "assessment")
}

enum class FilterOption(val label: String) {
    ALL("All Items"),
    HIGHLIGHTED_ONLY("Highlighted / Suspicious"),
    CONFIRMED_ONLY("Confirmed Present"),
    RESTRICTED_ONLY("Restricted / Android 14+ Protected")
}

class HardwareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DIContainer.provideRepository(application)

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _report = MutableStateFlow<FullHardwareAnalysisReport?>(null)
    val report: StateFlow<FullHardwareAnalysisReport?> = _report.asStateFlow()

    private val _selectedTab = MutableStateFlow(ModuleTab.DASHBOARD)
    val selectedTab: StateFlow<ModuleTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    val scanHistory: StateFlow<List<ScanReportEntity>> = repository.scanHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        runFullScan()
        repository.schedulePeriodicBackgroundScan()
    }

    fun runFullScan() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.performComprehensiveScan()
                _report.value = res
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTab(tab: ModuleTab) {
        _selectedTab.value = tab
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun clearScanHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun exportReport(format: String) {
        val r = _report.value ?: return
        repository.exportAndShareReport(r, format)
    }
}
