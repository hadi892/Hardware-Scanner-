package com.example.data.scanners

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.example.data.models.AudioCapabilityModel

object AudioScanner {

    fun scan(context: Context): AudioCapabilityModel {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val modeStr = when (audioManager.mode) {
            AudioManager.MODE_NORMAL -> "Normal"
            AudioManager.MODE_RINGTONE -> "Ringtone"
            AudioManager.MODE_IN_CALL -> "In Call"
            AudioManager.MODE_IN_COMMUNICATION -> "In Communication"
            else -> "Unknown Mode (${audioManager.mode})"
        }

        val outputs = mutableListOf<String>()
        val inputs = mutableListOf<String>()
        var fmDetected = false
        val fmDetails = StringBuilder()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devOutputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (dev in devOutputs) {
                val typeName = getAudioDeviceTypeName(dev.type)
                outputs.add("$typeName (${dev.productName})")
                if (dev.type == 19 /* TYPE_FM */ || dev.type == 20 /* TYPE_FM_TUNER */ || dev.productName.toString().contains("fm", ignoreCase = true)) {
                    fmDetected = true
                    fmDetails.append("Output device detected: $typeName (${dev.productName}). ")
                }
            }

            val devInputs = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            for (dev in devInputs) {
                val typeName = getAudioDeviceTypeName(dev.type)
                inputs.add("$typeName (${dev.productName})")
                if (dev.type == 19 || dev.type == 20 || dev.productName.toString().contains("fm", ignoreCase = true)) {
                    fmDetected = true
                    fmDetails.append("Input tuner detected: $typeName (${dev.productName}). ")
                }
            }
        } else {
            outputs.add("Legacy Audio Output")
            inputs.add("Legacy Audio Input")
        }

        // Test FM audio parameters
        val fmParamTest = try {
            audioManager.getParameters("fm_radio") + " " + audioManager.getParameters("fm_status")
        } catch (e: Exception) {
            ""
        }
        if (fmParamTest.isNotBlank() && !fmParamTest.contains("error", ignoreCase = true)) {
            fmDetected = true
            fmDetails.append("Audio parameter response: $fmParamTest. ")
        }

        val sampleRates = listOf(8000, 11025, 16000, 22050, 44100, 48000, 96000, 192000)

        return AudioCapabilityModel(
            mode = modeStr,
            isSpeakerphoneOn = try { audioManager.isSpeakerphoneOn } catch (e: Exception) { false },
            isBluetoothScoOn = try { audioManager.isBluetoothScoOn } catch (e: Exception) { false },
            outputDevices = if (outputs.isNotEmpty()) outputs else listOf("Built-in Speaker / Headphones"),
            inputDevices = if (inputs.isNotEmpty()) inputs else listOf("Built-in Microphone"),
            sampleRatesSupported = sampleRates,
            detectedFmRoutingIndication = fmDetected,
            fmRoutingEvidenceDetails = if (fmDetected) fmDetails.toString() else "No active FM tuner audio routing exposed via standard AudioManager interfaces."
        )
    }

    private fun getAudioDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Built-in Earpiece"
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in Speaker"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset (with Mic)"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
            AudioDeviceInfo.TYPE_HDMI -> "HDMI Audio"
            AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB Accessory Audio"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB DAC / Audio Device"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
            19 -> "FM Radio Output (TYPE_FM)"
            20 -> "FM Tuner Input (TYPE_FM_TUNER)"
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> "Built-in Microphone"
            else -> "Audio Device Type $type"
        }
    }
}
