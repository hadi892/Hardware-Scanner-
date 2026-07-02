package com.example.data.models

enum class CapabilityStatus {
    CONFIRMED,
    NOT_PRESENT,
    RESTRICTED_BY_ANDROID,
    SUSPICIOUS_EVIDENCE
}

data class SystemInfoModel(
    val androidVersion: String,
    val apiLevel: Int,
    val kernelVersion: String,
    val buildFingerprint: String,
    val buildTags: String,
    val buildType: String,
    val board: String,
    val brand: String,
    val manufacturer: String,
    val model: String,
    val product: String,
    val device: String,
    val hardware: String,
    val bootloader: String,
    val radioVersion: String,
    val securityPatch: String,
    val supportedAbis: List<String>,
    val cpuArchitecture: String,
    val cpuCores: Int,
    val totalMemoryBytes: Long,
    val availableMemoryBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val gpuRenderer: String,
    val screenResolution: String,
    val displayMetricsInfo: String
)

data class PackageFeatureModel(
    val name: String,
    val category: String, // e.g. "Hardware", "Software", "Bluetooth", "WiFi", "FM", "Camera"
    val isAvailable: Boolean,
    val version: Int = 0
)

data class SystemPropertyItem(
    val key: String,
    val value: String,
    val isHighlighted: Boolean, // Highlighted if matches fm, radio, rf, tv, qualcomm, qcom, vendor, audio, dsp, media, tuner, broadcast
    val category: String
)

data class LibraryItem(
    val path: String,
    val fileName: String,
    val category: String, // fm, radio, qcom, qualcomm, rf, dsp, audio, media, broadcast, tv, dvb, atsc, isdb, tuner, decoder, encoder, frontend, demod
    val existsOnDisk: Boolean,
    val sizeBytes: Long = 0L
)

data class ConfigFileItem(
    val path: String,
    val keywordMatched: String,
    val previewExcerpt: String,
    val isReadable: Boolean
)

data class BinderServiceItem(
    val serviceName: String,
    val interfaceDescriptor: String,
    val isAccessible: Boolean,
    val purposeGuess: String,
    val isSuspiciousMultimedia: Boolean
)

data class HalCapabilityItem(
    val halName: String,
    val category: String, // Audio HAL, Bluetooth HAL, Camera HAL, Radio HAL, TV HAL, Tuner HAL, Media HAL
    val versionOrInfo: String,
    val status: CapabilityStatus
)

data class CodecItem(
    val name: String,
    val supportedTypes: List<String>,
    val isHardwareAccelerated: Boolean,
    val isEncoder: Boolean,
    val isVendorSpecific: Boolean
)

data class AudioCapabilityModel(
    val mode: String,
    val isSpeakerphoneOn: Boolean,
    val isBluetoothScoOn: Boolean,
    val outputDevices: List<String>,
    val inputDevices: List<String>,
    val sampleRatesSupported: List<Int>,
    val detectedFmRoutingIndication: Boolean,
    val fmRoutingEvidenceDetails: String
)

data class SensorItem(
    val name: String,
    val vendor: String,
    val version: Int,
    val type: Int,
    val resolution: Float,
    val maxRange: Float,
    val powerMa: Float,
    val isWakeUpSensor: Boolean,
    val fifoMaxEventCount: Int,
    val reportingMode: String
)

data class CameraItem(
    val cameraId: String,
    val facing: String,
    val resolutionsMegapixels: String,
    val hardwareLevel: String,
    val isLogicalMultiCamera: Boolean,
    val physicalCameraIds: List<String>,
    val vendorExtensionsSupported: List<String>
)

data class UsbCapabilityModel(
    val hasUsbHostFeature: Boolean,
    val hasUsbAccessoryFeature: Boolean,
    val attachedDevicesCount: Int,
    val attachedDeviceNames: List<String>,
    val supportsUsbAudio: Boolean,
    val supportsUsbVideo: Boolean,
    val supportsOtg: Boolean
)

data class NetworkCapabilityModel(
    val hasWifi: Boolean,
    val hasWifiDirect: Boolean,
    val hasWifiAware: Boolean,
    val hasBluetooth: Boolean,
    val hasBle: Boolean,
    val hasEthernetSupport: Boolean,
    val isVpnActive: Boolean,
    val isHotspotSupported: Boolean,
    val activeInterfaceNames: List<String>
)

data class QualcommComponentModel(
    val name: String,
    val category: String, // DSP, Hexagon, QMI, Diag, RIL, Audio DSP, RF Components, Modem Components
    val detectedLocation: String,
    val confidence: String
)

data class HiddenCapabilityScore(
    val capabilityName: String, // e.g., "FM Radio / Hardware Tuner", "Digital TV / DVB Tuner", "SDR / RF Receiver Support", "Qualcomm Hexagon DSP Audio", "Vendor Hidden APIs"
    val confidencePercentage: Int, // 0..100
    val evidenceFound: List<String>,
    val evidenceMissing: List<String>,
    val androidRestrictionsNote: String,
    val overallAssessment: String
)

data class FullHardwareAnalysisReport(
    val scanTimestamp: Long,
    val deviceTitle: String,
    val systemInfo: SystemInfoModel,
    val packageFeatures: List<PackageFeatureModel>,
    val systemProperties: List<SystemPropertyItem>,
    val scannedLibraries: List<LibraryItem>,
    val configurationFiles: List<ConfigFileItem>,
    val binderServices: List<BinderServiceItem>,
    val halCapabilities: List<HalCapabilityItem>,
    val codecs: List<CodecItem>,
    val audioCapability: AudioCapabilityModel,
    val sensors: List<SensorItem>,
    val cameras: List<CameraItem>,
    val usbCapability: UsbCapabilityModel,
    val networkCapability: NetworkCapabilityModel,
    val qualcommComponents: List<QualcommComponentModel>,
    val hiddenCapabilityScores: List<HiddenCapabilityScore>
)
