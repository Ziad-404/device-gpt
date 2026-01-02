package com.teamz.lab.debugger.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.teamz.lab.debugger.MainActivity
import com.teamz.lab.debugger.R

/**
 * Device Monitor Widget
 * 
 * Shows comprehensive device monitoring data on home screen (and lock screen if supported)
 * Uses data from existing SystemMonitorService - no duplicate monitoring
 * 
 * Note: Lock screen widgets are only available on Android 16+ on some devices.
 * This widget works on home screen for all Android 13+ devices.
 * 
 * Displays:
 * - Power consumption (Watts)
 * - Battery percentage
 * - RAM usage
 * - CPU info
 * - Network speed
 * - Health score
 * - Daily streak
 */
class LockScreenMonitorWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        android.util.Log.d("DeviceGPT_Widget", "onUpdate called for ${appWidgetIds.size} widget(s)")
        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Update widget when data changes
        if (intent.action == ACTION_UPDATE_WIDGET || 
            intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, LockScreenMonitorWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        android.util.Log.d("DeviceGPT_Widget", "Updating widget $appWidgetId")
        val views = RemoteViews(context.packageName, R.layout.widget_lock_screen_monitor)
        
        // Read data from SharedPreferences (stored by SystemMonitorService)
        val prefs = context.getSharedPreferences("lock_screen_widget_data", Context.MODE_PRIVATE)
        
        android.util.Log.d("DeviceGPT_Widget", "Reading widget data from SharedPreferences")
        
        val battery = prefs.getString("battery", "🔋 Battery: --") ?: "🔋 Battery: --"
        val ram = prefs.getString("ram", "🧠 RAM: --") ?: "🧠 RAM: --"
        val cpu = prefs.getString("cpu", "") ?: ""
        val download = prefs.getString("download", "📶 ↓ --") ?: "📶 ↓ --"
        val upload = prefs.getString("upload", "↑ --") ?: "↑ --"
        val latency = prefs.getString("latency", "") ?: ""
        val power = prefs.getString("power", "⚡ Power: --") ?: "⚡ Power: --"
        val thermal = prefs.getString("thermal", "🌡️ --") ?: "🌡️ --"
        val healthScore = prefs.getInt("health_score", 0)
        val streak = prefs.getInt("streak", 0)
        val lastUpdate = prefs.getLong("last_update", 0)
        
        // Format data for display
        val networkInfo = buildString {
            append(download)
            if (upload.isNotEmpty()) append(" • $upload")
            if (latency.isNotEmpty()) append(" • $latency")
        }
        
        val cpuInfo = if (cpu.isNotEmpty()) " • $cpu" else ""
        val ramInfo = ram + cpuInfo
        
        // Update widget views
        views.setTextViewText(R.id.widget_battery, battery)
        views.setTextViewText(R.id.widget_ram, ramInfo)
        views.setTextViewText(R.id.widget_network, networkInfo)
        views.setTextViewText(R.id.widget_power, power)
        views.setTextViewText(R.id.widget_thermal, thermal)
        views.setTextViewText(R.id.widget_health_score, "🏥 Health: $healthScore/10")
        views.setTextViewText(R.id.widget_streak, "🔥 Streak: $streak days")
        
        // Show last update time
        val timeAgo = if (lastUpdate > 0) {
            val secondsAgo = (System.currentTimeMillis() - lastUpdate) / 1000
            when {
                secondsAgo < 60 -> "Updated ${secondsAgo}s ago"
                secondsAgo < 3600 -> "Updated ${secondsAgo / 60}m ago"
                else -> "Updated ${secondsAgo / 3600}h ago"
            }
        } else {
            "No data"
        }
        views.setTextViewText(R.id.widget_last_update, timeAgo)
        
        // Set click intent to open app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        // Update widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views)
            android.util.Log.d("DeviceGPT_Widget", "Widget $appWidgetId updated successfully. Health: $healthScore/10, Streak: $streak days")
        } catch (e: Exception) {
            android.util.Log.e("DeviceGPT_Widget", "Error updating widget $appWidgetId", e)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.teamz.lab.debugger.UPDATE_LOCK_SCREEN_WIDGET"
        
        /**
         * Trigger widget update from SystemMonitorService
         */
        fun updateWidget(context: Context) {
            android.util.Log.d("DeviceGPT_Widget", "Triggering widget update from SystemMonitorService")
            try {
                val intent = Intent(context, LockScreenMonitorWidget::class.java).apply {
                    action = ACTION_UPDATE_WIDGET
                }
                context.sendBroadcast(intent)
                android.util.Log.d("DeviceGPT_Widget", "Widget update broadcast sent successfully")
            } catch (e: Exception) {
                android.util.Log.e("DeviceGPT_Widget", "Error sending widget update broadcast", e)
            }
        }
    }
}

