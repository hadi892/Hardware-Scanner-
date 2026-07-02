package com.example.data.scanners

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.data.models.NetworkCapabilityModel
import java.net.NetworkInterface

object NetworkScanner {

    fun scan(context: Context): NetworkCapabilityModel {
        val pm = context.packageManager
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        var isVpn = false
        var hasEth = false
        val activeIfaces = mutableListOf<String>()

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces != null && interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isUp && !iface.isLoopback) {
                    activeIfaces.add("${iface.name} (${iface.displayName})")
                    if (iface.name.startsWith("tun") || iface.name.startsWith("ppp") || iface.name.startsWith("tap")) {
                        isVpn = true
                    }
                    if (iface.name.startsWith("eth")) {
                        hasEth = true
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val activeNet = cm.activeNetwork
                if (activeNet != null) {
                    val caps = cm.getNetworkCapabilities(activeNet)
                    if (caps != null) {
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) isVpn = true
                        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) hasEth = true
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        return NetworkCapabilityModel(
            hasWifi = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            hasWifiDirect = pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT),
            hasWifiAware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) pm.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE) else false,
            hasBluetooth = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH),
            hasBle = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE),
            hasEthernetSupport = hasEth || pm.hasSystemFeature("android.hardware.ethernet"),
            isVpnActive = isVpn,
            isHotspotSupported = pm.hasSystemFeature(PackageManager.FEATURE_WIFI),
            activeInterfaceNames = if (activeIfaces.isNotEmpty()) activeIfaces else listOf("No active network interfaces detected")
        )
    }
}
