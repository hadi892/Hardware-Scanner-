package com.example.data.scanners

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import com.example.data.models.SensorItem

object SensorScanner {

    fun scan(context: Context): List<SensorItem> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val allSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)

        return allSensors.map { s ->
            val reportingModeStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                when (s.reportingMode) {
                    Sensor.REPORTING_MODE_CONTINUOUS -> "Continuous"
                    Sensor.REPORTING_MODE_ON_CHANGE -> "On Change"
                    Sensor.REPORTING_MODE_ONE_SHOT -> "One Shot"
                    Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> "Special Trigger"
                    else -> "Mode ${s.reportingMode}"
                }
            } else "N/A"

            SensorItem(
                name = s.name ?: "Unknown Sensor",
                vendor = s.vendor ?: "Unknown Vendor",
                version = s.version,
                type = s.type,
                resolution = s.resolution,
                maxRange = s.maximumRange,
                powerMa = s.power,
                isWakeUpSensor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) s.isWakeUpSensor else false,
                fifoMaxEventCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) s.fifoMaxEventCount else 0,
                reportingMode = reportingModeStr
            )
        }.sortedBy { it.name }
    }
}
