package com.agon.app.util

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Helper class for WiFi-related prompts and actions.
 */
object WifiPromptHelper {

    /**
     * Check if WiFi is enabled.
     */
    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        return wifiManager?.isWifiEnabled == true
    }

    /**
     * Open WiFi settings.
     */
    fun openWifiSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }

    /**
     * Open wireless settings (includes hotspot).
     * FIX: Use ACTION_WIRELESS_SETTINGS instead of ACTION_TETHER_SETTINGS
     */
    fun openTetherSettings(context: Context) {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)
    }
}
