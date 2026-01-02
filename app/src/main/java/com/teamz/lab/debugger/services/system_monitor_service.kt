package com.teamz.lab.debugger.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.teamz.lab.debugger.MainActivity
import kotlinx.coroutines.*
import com.teamz.lab.debugger.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import com.teamz.lab.debugger.utils.PowerAlerts
import com.teamz.lab.debugger.utils.PowerConsumptionAggregator
import com.teamz.lab.debugger.utils.HealthScoreUtils
import com.teamz.lab.debugger.utils.DeviceSleepTracker
import com.teamz.lab.debugger.R

/**
 * SystemMonitorService - Real Device Data Monitoring
 * 
 * ✅ 100% REAL DEVICE DATA - NO ESTIMATES OR SIMULATIONS
 * 
 * All data sources use REAL Android system APIs and real network tests:
 * 
 * 1. getRamUsage() - Uses ActivityManager.getMemoryInfo() (REAL system memory)
 * 2. getCompactCpuInfo() - Reads from /sys/devices/system/cpu/ (REAL CPU frequencies)
 * 3. getCompactPowerState() - Uses PowerManager API (REAL power/thermal state)
 * 4. getCompactBatteryStatus() - Uses BatteryManager API (REAL voltage/current)
 * 5. getNetworkDownloadSpeed() - Actually downloads 10MB from Cloudflare (REAL network test)
 * 6. getNetworkUploadSpeed() - Actually uploads 2MB to httpbin (REAL network test)
 * 7. getCompactLatency() - Executes ping command (REAL network latency)
 * 8. getCompactFpsAndDropRate() - Uses Choreographer API (REAL frame monitoring)
 * 9. PowerConsumptionUtils.getPowerConsumptionData() - Uses BatteryManager API (REAL power)
 * 
 * Research Compliance:
 * - All data uses real Android system APIs
 * - Battery power uses physics formula: P = V × I
 * - Network tests actually transfer data (not estimated)
 * - No simulated or estimated values
 * 
 * See: SYSTEM_MONITOR_SERVICE_VALIDATION.md for detailed validation
 * See: SystemMonitorServiceRealDeviceValidationTest.kt for test coverage
 */
class SystemMonitorService : Service() {
    private val channelId = "system_monitor_channel"
    private val notificationId = 1001
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fpsDataFlow = MutableStateFlow("Initializing...")

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification("Initializing system monitor..."))
        startMonitoring()
        setMonitorServiceRunning(true)
    }


    /**
     * Start monitoring system metrics using REAL device data
     * 
     * All data sources use REAL Android APIs:
     * - Network speeds: Real HTTP transfers (download/upload)
     * - RAM: Real ActivityManager.getMemoryInfo()
     * - CPU: Real /sys/devices/system/cpu/ files
     * - Power/Thermal: Real PowerManager API
     * - Battery: Real BatteryManager API (P = V × I)
     * - Latency: Real ping command
     * - FPS: Real Choreographer frame callbacks
     * - Power Consumption: Real BatteryManager API
     */
    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                try {
                    val context = applicationContext
                    
                    // Update device sleep tracker
                    android.util.Log.d("DeviceGPT_Service", "Updating device sleep tracker")
                    DeviceSleepTracker.updateSleepState(context)

                    // All these functions use REAL device data (no estimates):
                    val downloadDeferred = async { getNetworkDownloadSpeed() } // Real HTTP download
                    val uploadDeferred = async { getNetworkUploadSpeed() } // Real HTTP upload
                    val ramDeferred = async { getRamUsage(context) } // Real ActivityManager
                    val cpuInfoDeferred = async { getCompactCpuInfo() } // Real sysfs files
                    val thermalDeferred = async { getCompactPowerState(context) } // Real PowerManager
                    val batteryDeferred = async { getCompactBatteryStatus(context) } // Real BatteryManager
                    val latencyDeferred = async { getCompactLatency() } // Real ping command
                    val powerConsumptionDeferred = async { PowerConsumptionUtils.getCompactPowerConsumption(context) } // Real BatteryManager
                    
                    // Get power data for alerts and trends
                    val powerDataDeferred = async { 
                        try {
                            PowerConsumptionUtils.getPowerConsumptionData(context)
                        } catch (e: Exception) {
                            com.teamz.lab.debugger.utils.ErrorHandler.handleError(
                                e,
                                context = "SystemMonitorService.getPowerConsumptionData"
                            )
                            null
                        }
                    }
                    val aggregatedStats = PowerConsumptionAggregator.aggregatedStatsFlow.value

                    withContext(Dispatchers.Main) {
                        getCompactFpsAndDropRate { fpsData ->
                            fpsDataFlow.value = fpsData
                        }
                    }

                    val cpuInfo = cpuInfoDeferred.await()
                    val ramInfo =
                        ramDeferred.await() + if (cpuInfo.isNotBlank()) " • $cpuInfo" else ""
                    
                    // Get power alerts and trend
                    val powerData = powerDataDeferred.await()
                    val powerAlerts = if (powerData != null && aggregatedStats != null) {
                        PowerAlerts.checkAlerts(context, powerData, aggregatedStats)
                    } else {
                        emptyList()
                    }
                    
                    // Build power info with alerts and trends
                    val powerInfo = buildString {
                        append(powerConsumptionDeferred.await())
                        aggregatedStats?.let { stats ->
                            when (stats.powerTrend) {
                                PowerConsumptionAggregator.PowerTrend.INCREASING -> append(" 📈")
                                PowerConsumptionAggregator.PowerTrend.DECREASING -> append(" 📉")
                                PowerConsumptionAggregator.PowerTrend.STABLE -> append(" 📊")
                                else -> {}
                            }
                        }
                        if (powerAlerts.isNotEmpty()) {
                            val criticalAlerts = powerAlerts.filter { it.severity == PowerAlerts.Severity.CRITICAL }
                            if (criticalAlerts.isNotEmpty()) {
                                append(" ⚠️")
                            }
                        }
                    }

                    val compactContent = """
🔋 ${batteryDeferred.await()}
🧠 Ram : $ramInfo
📶 ${downloadDeferred.await()} ↓ • ${uploadDeferred.await()} ↑ • ${latencyDeferred.await()}
🎮 ${fpsDataFlow.value}
🌡️ ${thermalDeferred.await()}
$powerInfo
""".trimIndent()

                    // Store data for DeviceGPT widget
                    android.util.Log.d("DeviceGPT_Service", "Collecting data for widget update")
                    storeDataForWidget(
                        context,
                        batteryDeferred.await(),
                        ramInfo,
                        cpuInfo,
                        downloadDeferred.await(),
                        uploadDeferred.await(),
                        latencyDeferred.await(),
                        powerInfo,
                        thermalDeferred.await()
                    )

                    // 🔄 Update Notification on MAIN thread

                    withContext(Dispatchers.Main) {
                        val manager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(notificationId, buildNotification(compactContent))
                        android.util.Log.d("DeviceGPT_Service", "Notification updated")
                        
                        // Update DeviceGPT widget (home screen / lock screen if supported)
                        android.util.Log.d("DeviceGPT_Service", "Triggering widget update")
                        com.teamz.lab.debugger.widgets.LockScreenMonitorWidget.updateWidget(context)
                    }

                } catch (e: Exception) {
                    handleError(e)
                }

                delay(30000) // ⏳ wait 30s
            }
        }
    }


    private fun buildNotification(content: String) =
        // Create an intent to launch MainActivity when notification is tapped
        Intent(this, MainActivity::class.java).let { intent ->
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            NotificationCompat.Builder(this, channelId)
                .setContentTitle("📊 Live Device & Network Status")
                .setContentText(getString(R.string.notification_watching_over))
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentIntent(pendingIntent)  // Launch MainActivity on click
                .setOngoing(true)
                .build()
        }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "System Monitor"
            val description = "Live device and network metrics"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Store monitoring data in SharedPreferences for DeviceGPT widget to read
     */
    private fun storeDataForWidget(
        context: Context,
        battery: String,
        ram: String,
        cpu: String,
        download: String,
        upload: String,
        latency: String,
        power: String,
        thermal: String
    ) {
        android.util.Log.d("DeviceGPT_Service", "Storing widget data: battery=$battery, power=$power, ram=$ram")
        try {
            // Get health score and streak before storing
            val healthScore = HealthScoreUtils.calculateDailyHealthScore(context)
            val streak = HealthScoreUtils.getDailyStreak(context)
            
            val prefs = context.getSharedPreferences("lock_screen_widget_data", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("battery", battery)
                putString("ram", ram)
                putString("cpu", cpu)
                putString("download", download)
                putString("upload", upload)
                putString("latency", latency)
                putString("power", power)
                putString("thermal", thermal)
                putLong("last_update", System.currentTimeMillis())
                putInt("health_score", healthScore)
                putInt("streak", streak)
                apply()
            }
            android.util.Log.d("DeviceGPT_Service", "Widget data stored successfully. Health: $healthScore/10, Streak: $streak days")
        } catch (e: Exception) {
            android.util.Log.e("DeviceGPT_Service", "Error storing widget data", e)
        }
    }

    override fun onDestroy() {
        setMonitorServiceRunning(false)
        scope.cancel()
        super.onDestroy()
    }
}


fun Context.startSystemMonitorService() {
    val intent = Intent(this, SystemMonitorService::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ContextCompat.startForegroundService(
            this, intent
        )
    } else {
        this.startService(intent)
    }
}

fun Context.setDoNotAskMeAgain(doNotAskMeAgain: Boolean) {
    getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("don-not-ask-me-again", doNotAskMeAgain)
        .apply()
}

fun Context.isDoNotAskMeAgain(): Boolean {
    return getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .getBoolean("don-not-ask-me-again", false)
}

fun Context.setMonitorServiceRunning(isRunning: Boolean) {
    getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("running", isRunning)
        .apply()
}

fun Context.isMonitorServiceFlaggedAsRunning(): Boolean {
    return getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .getBoolean("running", false)
}

fun Context.setUserEnableMonitoringService(isRunning: Boolean) {
    getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("user_enable_monitor_service", isRunning)
        .apply()
}

fun Context.isUserEnableMonitoringService(): Boolean {
    return getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .getBoolean("user_enable_monitor_service", false)
}

fun Context.isUserFirstTime(): Boolean {
    return getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .getBoolean("is_user_first_time", true)

}

fun Context.setUserFirstTime(isFirstTime: Boolean) {
    return getSharedPreferences("monitor_service", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("is_user_first_time", isFirstTime)
        .apply()
}


fun Context.isSystemMonitorServiceActuallyRunning(): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
    return runningServices.any {
        it.service.className == SystemMonitorService::class.java.name
    }
}


fun Context.isSystemMonitorRunning(): Boolean {
    return isMonitorServiceFlaggedAsRunning() && isSystemMonitorServiceActuallyRunning()
}


fun Context.userHasAlreadyReviewed(): Boolean {
    return getSharedPreferences("review_prefs", Context.MODE_PRIVATE)
        .getBoolean("has_reviewed", false)
}


fun Context.setAlreadyReviewed(hasReview: Boolean) {
    getSharedPreferences("review_prefs", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("has_reviewed", hasReview)
        .apply()
}

fun Context.stopSystemMonitorService() {
    stopService(Intent(this, SystemMonitorService::class.java))
}
