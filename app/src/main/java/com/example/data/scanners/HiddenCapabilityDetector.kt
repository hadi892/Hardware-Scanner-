package com.example.data.scanners

import com.example.data.models.AudioCapabilityModel
import com.example.data.models.BinderServiceItem
import com.example.data.models.ConfigFileItem
import com.example.data.models.HalCapabilityItem
import com.example.data.models.HiddenCapabilityScore
import com.example.data.models.LibraryItem
import com.example.data.models.PackageFeatureModel
import com.example.data.models.QualcommComponentModel
import com.example.data.models.SystemPropertyItem

object HiddenCapabilityDetector {

    fun analyze(
        packageFeatures: List<PackageFeatureModel>,
        systemProperties: List<SystemPropertyItem>,
        libraries: List<LibraryItem>,
        configs: List<ConfigFileItem>,
        binders: List<BinderServiceItem>,
        hals: List<HalCapabilityItem>,
        audio: AudioCapabilityModel,
        qualcomm: List<QualcommComponentModel>
    ): List<HiddenCapabilityScore> {
        val scores = mutableListOf<HiddenCapabilityScore>()

        // 1. FM Radio / Broadcast Tuner
        scores.add(
            analyzeFmRadio(packageFeatures, systemProperties, libraries, configs, binders, hals, audio)
        )

        // 2. Digital TV / DVB Tuner
        scores.add(
            analyzeDigitalTv(packageFeatures, systemProperties, libraries, configs, binders, hals)
        )

        // 3. RF Receiver / SDR Support
        scores.add(
            analyzeRfReceiver(packageFeatures, systemProperties, libraries, configs, binders, qualcomm)
        )

        // 4. External Tuner Support (USB Dongles / SDR / DVB-T USB)
        scores.add(
            analyzeExternalTuner(packageFeatures, libraries, configs)
        )

        // 5. Audio DSP & Hardware Offload
        scores.add(
            analyzeAudioDsp(systemProperties, libraries, configs, binders, qualcomm)
        )

        // 6. Vendor Hidden APIs & Proprietary IPC
        scores.add(
            analyzeVendorHiddenApis(systemProperties, libraries, binders, qualcomm)
        )

        return scores
    }

    private fun analyzeFmRadio(
        features: List<PackageFeatureModel>,
        props: List<SystemPropertyItem>,
        libs: List<LibraryItem>,
        configs: List<ConfigFileItem>,
        binders: List<BinderServiceItem>,
        hals: List<HalCapabilityItem>,
        audio: AudioCapabilityModel
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val hasPmFeature = features.any { it.name.contains("broadcastradio") || it.name.contains("fmradio") && it.isAvailable }
        if (hasPmFeature) {
            score += 45
            found.add("PackageManager flag android.hardware.broadcastradio/fmradio confirmed present.")
        } else {
            missing.add("Official PackageManager FM/Broadcast feature flag not advertised.")
        }

        val fmProps = props.filter { it.key.contains("fm", true) || it.value.contains("fm", true) }
        if (fmProps.isNotEmpty()) {
            score += 25
            found.add("Found ${fmProps.size} system properties referencing FM radio (e.g. ${fmProps.first().key}).")
        } else {
            missing.add("No active system properties explicitly enabling FM tuner routing found.")
        }

        val fmLibs = libs.filter { it.fileName.contains("fm", true) && it.existsOnDisk }
        if (fmLibs.isNotEmpty()) {
            score += 20
            found.add("Found vendor FM shared library on disk: ${fmLibs.first().fileName}.")
        } else {
            missing.add("No vendor native libraries named *fm* found in /vendor/lib or /system/lib64.")
        }

        val fmHal = hals.any { it.halName.contains("broadcastradio") && it.status.name == "CONFIRMED" }
        if (fmHal) {
            score += 20
            found.add("BroadcastRadio HAL confirmed accessible.")
        }

        if (audio.detectedFmRoutingIndication) {
            score += 15
            found.add("AudioManager reported active routing indications or mixer ports for FM tuner audio.")
        } else {
            missing.add("AudioManager does not advertise TYPE_FM (19) or TYPE_FM_TUNER (20) endpoints.")
        }

        score = score.coerceIn(0, 100)

        val assessment = when {
            score >= 70 -> "HIGH PROBABILITY: Physical FM tuner hardware exists and is supported by SoC/HAL, though may require vendor headset antenna or proprietary OEM app."
            score >= 35 -> "MODERATE PROBABILITY / DORMANCY: SoC chipset supports FM broadcast radio, but OEM disabled public APIs or omitted antenna routing pins on this tablet variant."
            else -> "UNLIKELY / NOT PRESENT: No concrete hardware evidence of active FM receiver circuitry found on accessible Android interfaces."
        }

        return HiddenCapabilityScore(
            capabilityName = "FM Radio / Broadcast Tuner",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No direct positive evidence discovered."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Android 14+ severely restricts direct /dev/radio0 access from standard sandbox apps. Only HAL service bindings or vendor OEM apps can interact with physical radio tuners.",
            overallAssessment = assessment
        )
    }

    private fun analyzeDigitalTv(
        features: List<PackageFeatureModel>,
        props: List<SystemPropertyItem>,
        libs: List<LibraryItem>,
        configs: List<ConfigFileItem>,
        binders: List<BinderServiceItem>,
        hals: List<HalCapabilityItem>
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val tvHal = hals.any { it.halName.contains("tv.tuner") && it.status.name == "CONFIRMED" }
        if (tvHal) {
            score += 50
            found.add("Tuner HAL feature flag android.hardware.tv.tuner confirmed active.")
        } else {
            missing.add("android.hardware.tv.tuner feature flag is not enabled.")
        }

        val tvLibs = libs.filter { (it.fileName.contains("dvb", true) || it.fileName.contains("isdb", true) || it.fileName.contains("atsc", true)) && it.existsOnDisk }
        if (tvLibs.isNotEmpty()) {
            score += 30
            found.add("Found DVB/ATSC/ISDB decoder library on disk: ${tvLibs.first().fileName}.")
        } else {
            missing.add("No digital TV demodulator or tuner shared libraries (DVB/ISDB/ATSC) found.")
        }

        val tvBinders = binders.filter { it.serviceName.contains("tv_tuner", true) }
        if (tvBinders.isNotEmpty()) {
            score += 25
            found.add("Binder service '${tvBinders.first().serviceName}' registered with ServiceManager.")
        } else {
            missing.add("No tv_tuner Binder service registered.")
        }

        score = score.coerceIn(0, 100)

        val assessment = when {
            score >= 60 -> "CONFIRMED / HIGH PROBABILITY: Digital TV tuner pipeline (ISDB-T / DVB-T) is present in system architecture."
            score >= 25 -> "DORMANT PIPELINE: Android Media Framework contains standard Tuner HAL hooks, but no active RF tuner chip detected on this tablet board."
            else -> "NOT SUPPORTED: No Digital TV hardware demodulator components found."
        }

        return HiddenCapabilityScore(
            capabilityName = "Digital TV / DVB Tuner",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No positive evidence found."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Accessing Tuner HAL requires android.permission.ACCESS_TV_TUNER which is signature/system level on non-Android TV builds.",
            overallAssessment = assessment
        )
    }

    private fun analyzeRfReceiver(
        features: List<PackageFeatureModel>,
        props: List<SystemPropertyItem>,
        libs: List<LibraryItem>,
        configs: List<ConfigFileItem>,
        binders: List<BinderServiceItem>,
        qualcomm: List<QualcommComponentModel>
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val rfQcom = qualcomm.filter { it.category.contains("RF") || it.category.contains("Modem") || it.category.contains("QMI") }
        if (rfQcom.isNotEmpty()) {
            score += 40
            found.add("Detected ${rfQcom.size} active RF/QMI transceiver components (e.g. ${rfQcom.first().name}).")
        } else {
            missing.add("No direct QMI/RF diagnostic components accessible.")
        }

        val diagBinders = binders.filter { it.serviceName.contains("phone") || it.serviceName.contains("ril") }
        if (diagBinders.isNotEmpty()) {
            score += 20
            found.add("Telephony/RIL radio services registered and active.")
        }

        val rfProps = props.filter { it.key.contains("ril.", true) || it.key.contains("radio.", true) }
        if (rfProps.isNotEmpty()) {
            score += 15
            found.add("Found ${rfProps.size} baseband/RIL radio properties.")
        }

        score = score.coerceIn(0, 100)

        return HiddenCapabilityScore(
            capabilityName = "SDR / RF Receiver Support",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No specific SDR evidence found."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Baseband radio raw IQ sampling is blocked by Qualcomm trustzone and modem isolation. Raw SDR requires USB OTG dongle (RTL-SDR).",
            overallAssessment = if (score >= 50) "MODERATE TO HIGH: Active baseband transceiver and QMI interface present. Raw RF capture requires external USB RTL-SDR hardware." else "LOW: Standard cellular/Wi-Fi radio stack only."
        )
    }

    private fun analyzeExternalTuner(
        features: List<PackageFeatureModel>,
        libs: List<LibraryItem>,
        configs: List<ConfigFileItem>
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val hasUsbHost = features.any { it.name.contains("usb.host") && it.isAvailable }
        if (hasUsbHost) {
            score += 65
            found.add("USB Host (OTG) confirmed available. Allows plugging in external USB SDR/DVB-T tuners.")
        } else {
            missing.add("USB Host feature not advertised.")
        }

        val uvcLib = libs.any { it.fileName.contains("uvc", true) || it.fileName.contains("usb", true) }
        if (uvcLib) {
            score += 25
            found.add("Found native USB video/stream support drivers.")
        }

        score = score.coerceIn(0, 100)

        return HiddenCapabilityScore(
            capabilityName = "External USB Tuner / SDR Support",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No positive evidence found."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Standard user apps can fully utilize USB SDR / DVB-T dongles (e.g. RTL-SDR v3) via Android UsbManager host permission requests without root.",
            overallAssessment = if (score >= 60) "FULLY SUPPORTED: Device supports USB OTG Host mode. Users can attach RTL-SDR or DVB-T USB receivers and run SDR apps directly." else "RESTRICTED: USB Host support not confirmed."
        )
    }

    private fun analyzeAudioDsp(
        props: List<SystemPropertyItem>,
        libs: List<LibraryItem>,
        configs: List<ConfigFileItem>,
        binders: List<BinderServiceItem>,
        qualcomm: List<QualcommComponentModel>
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val dspQcom = qualcomm.filter { it.category.contains("DSP") || it.category.contains("Audio DSP") }
        if (dspQcom.isNotEmpty()) {
            score += 45
            found.add("Found Qualcomm Hexagon / ADSP / CDSP libraries (${dspQcom.first().name}).")
        } else {
            missing.add("No specific Hexagon ADSP libraries listed.")
        }

        val audioConfigs = configs.filter { it.path.contains("audio_policy", true) && it.isReadable }
        if (audioConfigs.isNotEmpty()) {
            score += 30
            found.add("Successfully read HAL hardware audio policy configuration (${audioConfigs.first().path}).")
        } else {
            missing.add("audio_policy_configuration.xml restricted or unreadable.")
        }

        val offloadProps = props.filter { it.key.contains("offload", true) || it.key.contains("tunnel", true) }
        if (offloadProps.isNotEmpty()) {
            score += 20
            found.add("Hardware audio offload / DSP tunnel decoding enabled via system properties.")
        }

        score = score.coerceIn(0, 100)

        return HiddenCapabilityScore(
            capabilityName = "Qualcomm Hexagon DSP Audio Offload",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No specific DSP evidence found."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Direct DSP firmware execution requires OEM vendor signing. Audio offload is utilized automatically by Android AudioFlinger.",
            overallAssessment = if (score >= 60) "HIGH CONFIDENCE: Dedicated hardware Audio DSP / Hexagon co-processor active on device." else "STANDARD: Software or standard HAL audio processing."
        )
    }

    private fun analyzeVendorHiddenApis(
        props: List<SystemPropertyItem>,
        libs: List<LibraryItem>,
        binders: List<BinderServiceItem>,
        qualcomm: List<QualcommComponentModel>
    ): HiddenCapabilityScore {
        var score = 0
        val found = mutableListOf<String>()
        val missing = mutableListOf<String>()

        val vendorBinders = binders.filter { it.serviceName.startsWith("vendor.") || it.serviceName.contains("qcom") }
        if (vendorBinders.isNotEmpty()) {
            score += 50
            found.add("Encountered ${vendorBinders.size} vendor-specific Binder IPC interfaces (e.g. ${vendorBinders.first().serviceName}).")
        } else {
            missing.add("No exposed vendor.* binder services enumerated.")
        }

        val vendorProps = props.filter { it.key.startsWith("vendor.") || it.key.startsWith("persist.vendor.") }
        if (vendorProps.isNotEmpty()) {
            score += 30
            found.add("Discovered ${vendorProps.size} vendor system properties.")
        }

        if (qualcomm.isNotEmpty()) {
            score += 20
            found.add("Qualcomm proprietary QMI/Diag diagnostic framework detected.")
        }

        score = score.coerceIn(0, 100)

        return HiddenCapabilityScore(
            capabilityName = "Vendor Hidden APIs & Hardware Daemons",
            confidencePercentage = score,
            evidenceFound = if (found.isNotEmpty()) found else listOf("No vendor hidden APIs enumerated."),
            evidenceMissing = missing,
            androidRestrictionsNote = "Android HIDL/AIDL Treble rules isolate vendor daemons. Standard apps cannot call hidden vendor binder interfaces without platform UID.",
            overallAssessment = if (score >= 65) "CONFIRMED EXTENSIVE VENDOR HAL: Rich set of OEM proprietary hardware daemons present." else "MINIMAL: Standard clean Android HAL implementation."
        )
    }
}
