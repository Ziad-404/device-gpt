package com.teamz.lab.debugger.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.PowerManager
import androidx.core.content.edit
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

/**
 * Device Sleep Tracker
 * 
 * Automatically tracks device sleep patterns (when screen auto-locks/wakes)
 * - NOT user sleep tracking (like health apps)
 * - Tracks DEVICE sleep/wake cycles
 * - Unique feature based on keyword: "automatic sleep tracker" (score: 0.259)
 * 
 * Features:
 * - Automatic tracking (no user input needed)
 * - Sleep duration calculation
 * - Wake frequency tracking
 * - Sleep efficiency metrics
 * - Daily sleep pattern history
 */
object DeviceSleepTracker {
    
    private const val PREFS_NAME = "device_sleep_tracker_prefs"
    private const val KEY_LAST_SCREEN_STATE = "last_screen_state"
    private const val KEY_LAST_STATE_CHANGE_TIME = "last_state_change_time"
    private const val KEY_SLEEP_SESSIONS = "sleep_sessions"
    private const val KEY_WAKE_SESSIONS = "wake_sessions"
    private const val KEY_TOTAL_SLEEP_TIME = "total_sleep_time"
    private const val KEY_TOTAL_WAKE_TIME = "total_wake_time"
    private const val KEY_LAST_SLEEP_DATE = "last_sleep_date"
    
    /**
     * Handle screen ON event (device woke up)
     * Note: This is no longer used - SystemMonitorService.updateSleepState() handles state changes
     * Kept for backward compatibility
     */
    @Deprecated("Use updateSleepState() instead - it's called periodically by SystemMonitorService")
    fun handleScreenOn(context: Context) {
        val currentTime = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastState = prefs.getBoolean(KEY_LAST_SCREEN_STATE, true) // Default to awake
        val lastStateChangeTime = prefs.getLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        
        android.util.Log.d("DeviceGPT_SleepTracker", "Screen ON - Last state: ${if (lastState) "Awake" else "Sleeping"}")
        
        if (!lastState) {
            // Was sleeping, now awake - record wake event
            val sleepDuration = currentTime - lastStateChangeTime
            android.util.Log.d("DeviceGPT_SleepTracker", "Device woke up after ${sleepDuration / 1000}s of sleep")
            recordWakeEvent(context, sleepDuration)
        }
        
        // Update state to awake
        prefs.edit {
            putBoolean(KEY_LAST_SCREEN_STATE, true)
            putLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        }
        android.util.Log.d("DeviceGPT_SleepTracker", "State updated to: Awake")
    }
    
    /**
     * Handle screen OFF event (device went to sleep)
     * Note: This is no longer used - SystemMonitorService.updateSleepState() handles state changes
     * Kept for backward compatibility
     */
    @Deprecated("Use updateSleepState() instead - it's called periodically by SystemMonitorService")
    fun handleScreenOff(context: Context) {
        val currentTime = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastState = prefs.getBoolean(KEY_LAST_SCREEN_STATE, true) // Default to awake
        val lastStateChangeTime = prefs.getLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        
        android.util.Log.d("DeviceGPT_SleepTracker", "Screen OFF - Last state: ${if (lastState) "Awake" else "Sleeping"}")
        
        if (lastState) {
            // Was awake, now sleeping - record sleep event
            val awakeDuration = currentTime - lastStateChangeTime
            android.util.Log.d("DeviceGPT_SleepTracker", "Device went to sleep after ${awakeDuration / 1000}s awake")
            recordSleepEvent(context, awakeDuration)
        }
        
        // Update state to sleeping
        prefs.edit {
            putBoolean(KEY_LAST_SCREEN_STATE, false)
            putLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        }
        android.util.Log.d("DeviceGPT_SleepTracker", "State updated to: Sleeping")
    }
    
    /**
     * Check and update device sleep state
     * Call this periodically (e.g., from SystemMonitorService or app lifecycle)
     * This is a fallback method that checks current state
     */
    fun updateSleepState(context: Context) {
        android.util.Log.d("DeviceGPT_SleepTracker", "Checking device sleep state (periodic check)")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = powerManager.isInteractive
        val currentTime = System.currentTimeMillis()
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastState = prefs.getBoolean(KEY_LAST_SCREEN_STATE, true) // Default to awake
        val lastStateChangeTime = prefs.getLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        
        android.util.Log.d("DeviceGPT_SleepTracker", "Current state: ${if (isInteractive) "Awake" else "Sleeping"}, Last state: ${if (lastState) "Awake" else "Sleeping"}")
        
        // If state changed (fallback - in case receiver missed an event)
        if (isInteractive != lastState) {
            val duration = currentTime - lastStateChangeTime
            android.util.Log.d("DeviceGPT_SleepTracker", "State mismatch detected! Duration in previous state: ${duration / 1000}s")
            
            if (!lastState && isInteractive) {
                // Device woke up (was sleeping, now awake)
                android.util.Log.d("DeviceGPT_SleepTracker", "Device woke up after ${duration / 1000}s of sleep (fallback detection)")
                recordWakeEvent(context, duration)
            } else if (lastState && !isInteractive) {
                // Device went to sleep (was awake, now sleeping)
                android.util.Log.d("DeviceGPT_SleepTracker", "Device went to sleep after ${duration / 1000}s awake (fallback detection)")
                recordSleepEvent(context, duration)
            }
            
            // Update state
            prefs.edit {
                putBoolean(KEY_LAST_SCREEN_STATE, isInteractive)
                putLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
            }
            android.util.Log.d("DeviceGPT_SleepTracker", "State saved: ${if (isInteractive) "Awake" else "Sleeping"}")
        } else {
            android.util.Log.d("DeviceGPT_SleepTracker", "No state change detected (state matches)")
        }
    }
    
    private fun recordSleepEvent(context: Context, awakeDuration: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Update total wake time
        val totalWakeTime = prefs.getLong(KEY_TOTAL_WAKE_TIME, 0) + awakeDuration
        val wakeSessions = prefs.getInt(KEY_WAKE_SESSIONS, 0) + 1
        
        prefs.edit {
            putLong(KEY_TOTAL_WAKE_TIME, totalWakeTime)
            putInt(KEY_WAKE_SESSIONS, wakeSessions)
            putString(KEY_LAST_SLEEP_DATE, today)
        }
        
        // Log sleep event
        android.util.Log.d("DeviceGPT_SleepTracker", "Device went to sleep. Was awake for ${awakeDuration / 1000}s")
        android.util.Log.d("DeviceGPT_SleepTracker", "Updated stats - Total wake time: ${totalWakeTime / 1000}s, Wake sessions: $wakeSessions")
    }
    
    private fun recordWakeEvent(context: Context, sleepDuration: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Update total sleep time
        val totalSleepTime = prefs.getLong(KEY_TOTAL_SLEEP_TIME, 0) + sleepDuration
        val sleepSessions = prefs.getInt(KEY_SLEEP_SESSIONS, 0) + 1
        
        prefs.edit {
            putLong(KEY_TOTAL_SLEEP_TIME, totalSleepTime)
            putInt(KEY_SLEEP_SESSIONS, sleepSessions)
        }
        
        // Log wake event
        android.util.Log.d("DeviceGPT_SleepTracker", "Device woke up. Was sleeping for ${sleepDuration / 1000}s")
        android.util.Log.d("DeviceGPT_SleepTracker", "Updated stats - Total sleep time: ${totalSleepTime / 1000}s, Sleep sessions: $sleepSessions")
    }
    
    /**
     * Get today's sleep statistics
     */
    fun getTodaySleepStats(context: Context): SleepStats {
        android.util.Log.d("DeviceGPT_SleepTracker", "Getting today's sleep statistics")
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSleepDate = prefs.getString(KEY_LAST_SLEEP_DATE, "")
        
        android.util.Log.d("DeviceGPT_SleepTracker", "Today: $today, Last sleep date: $lastSleepDate")
        
        // Reset if new day
        if (lastSleepDate != null && lastSleepDate != today) {
            android.util.Log.d("DeviceGPT_SleepTracker", "New day detected - resetting statistics")
            prefs.edit {
                putLong(KEY_TOTAL_SLEEP_TIME, 0)
                putLong(KEY_TOTAL_WAKE_TIME, 0)
                putInt(KEY_SLEEP_SESSIONS, 0)
                putInt(KEY_WAKE_SESSIONS, 0)
                putString(KEY_LAST_SLEEP_DATE, today)
            }
        } else if (lastSleepDate.isNullOrEmpty()) {
            // First time - set today's date
            prefs.edit {
                putString(KEY_LAST_SLEEP_DATE, today)
            }
        }
        
        val totalSleepTime = prefs.getLong(KEY_TOTAL_SLEEP_TIME, 0)
        val totalWakeTime = prefs.getLong(KEY_TOTAL_WAKE_TIME, 0)
        val sleepSessions = prefs.getInt(KEY_SLEEP_SESSIONS, 0)
        val wakeSessions = prefs.getInt(KEY_WAKE_SESSIONS, 0)
        
        val totalTime = totalSleepTime + totalWakeTime
        val sleepEfficiency = if (totalTime > 0) {
            (totalSleepTime.toDouble() / totalTime * 100).toInt()
        } else {
            0
        }
        
        android.util.Log.d("DeviceGPT_SleepTracker", "Stats calculated - Sleep: ${totalSleepTime / 1000}s, Wake: ${totalWakeTime / 1000}s, Efficiency: $sleepEfficiency%")
        
        return SleepStats(
            totalSleepTimeMs = totalSleepTime,
            totalWakeTimeMs = totalWakeTime,
            sleepSessions = sleepSessions,
            wakeSessions = wakeSessions,
            sleepEfficiency = sleepEfficiency,
            averageSleepDuration = if (sleepSessions > 0) totalSleepTime / sleepSessions else 0,
            averageWakeDuration = if (wakeSessions > 0) totalWakeTime / wakeSessions else 0
        )
    }
    
    /**
     * Initialize state when app starts
     * This ensures we have the correct state even if app was closed
     * Also schedules background tracking via WorkManager
     */
    fun initializeState(context: Context) {
        android.util.Log.d("DeviceGPT_SleepTracker", "Initializing sleep tracker state on app start")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = powerManager.isInteractive
        val currentTime = System.currentTimeMillis()
        
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastState = prefs.getBoolean(KEY_LAST_SCREEN_STATE, true)
        val lastStateChangeTime = prefs.getLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        
        android.util.Log.d("DeviceGPT_SleepTracker", "App start - Current: ${if (isInteractive) "Awake" else "Sleeping"}, Last saved: ${if (lastState) "Awake" else "Sleeping"}")
        
        // If state doesn't match, we might have missed events while app was closed
        // Calculate the time difference and update accordingly
        if (isInteractive != lastState) {
            val duration = currentTime - lastStateChangeTime
            android.util.Log.d("DeviceGPT_SleepTracker", "State mismatch on app start - missed ${duration / 1000}s")
            
            if (!lastState && isInteractive) {
                // Was sleeping, now awake - record wake event
                android.util.Log.d("DeviceGPT_SleepTracker", "Recording missed wake event: ${duration / 1000}s sleep")
                recordWakeEvent(context, duration)
            } else if (lastState && !isInteractive) {
                // Was awake, now sleeping - record sleep event
                android.util.Log.d("DeviceGPT_SleepTracker", "Recording missed sleep event: ${duration / 1000}s awake")
                recordSleepEvent(context, duration)
            }
        }
        
        // Update to current state
        prefs.edit {
            putBoolean(KEY_LAST_SCREEN_STATE, isInteractive)
            putLong(KEY_LAST_STATE_CHANGE_TIME, currentTime)
        }
        android.util.Log.d("DeviceGPT_SleepTracker", "State initialized to: ${if (isInteractive) "Awake" else "Sleeping"}")
        
        // Schedule background tracking via WorkManager (works even when app is closed)
        scheduleBackgroundTracking(context)
    }
    
    /**
     * Schedule periodic background tracking using WorkManager
     * This checks screen state every 15 minutes, even when app is closed
     */
    private fun scheduleBackgroundTracking(context: Context) {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .build()
            
            // Periodic work - minimum interval is 15 minutes
            val sleepTrackingWork = androidx.work.PeriodicWorkRequestBuilder<SleepTrackerWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag("sleep_tracker_work")
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.LINEAR,
                    5,
                    java.util.concurrent.TimeUnit.MINUTES
                )
                .build()
            
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sleep_tracker_periodic",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                sleepTrackingWork
            )
            android.util.Log.d("DeviceGPT_SleepTracker", "Background sleep tracking scheduled via WorkManager")
        } catch (e: Exception) {
            android.util.Log.e("DeviceGPT_SleepTracker", "Error scheduling background tracking", e)
        }
    }
    
    /**
     * Get current device state
     */
    fun isDeviceAwake(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isInteractive
    }
    
    data class SleepStats(
        val totalSleepTimeMs: Long,
        val totalWakeTimeMs: Long,
        val sleepSessions: Int,
        val wakeSessions: Int,
        val sleepEfficiency: Int, // Percentage
        val averageSleepDuration: Long, // Milliseconds
        val averageWakeDuration: Long // Milliseconds
    ) {
        fun formatSleepTime(): String {
            val hours = totalSleepTimeMs / (1000 * 60 * 60)
            val minutes = (totalSleepTimeMs % (1000 * 60 * 60)) / (1000 * 60)
            return "${hours}h ${minutes}m"
        }
        
        fun formatWakeTime(): String {
            val hours = totalWakeTimeMs / (1000 * 60 * 60)
            val minutes = (totalWakeTimeMs % (1000 * 60 * 60)) / (1000 * 60)
            return "${hours}h ${minutes}m"
        }
    }
}

/**
 * SleepTrackerWorker - Background worker that tracks device sleep/wake state
 * Runs every 15 minutes via WorkManager, even when app is closed
 */
class SleepTrackerWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    
    override fun doWork(): Result {
        android.util.Log.d("DeviceGPT_SleepTracker", "SleepTrackerWorker: Checking screen state in background")
        try {
            // Use updateSleepState to check and record any state changes
            DeviceSleepTracker.updateSleepState(applicationContext)
            android.util.Log.d("DeviceGPT_SleepTracker", "SleepTrackerWorker: Successfully updated sleep state")
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("DeviceGPT_SleepTracker", "SleepTrackerWorker: Error updating sleep state", e)
            return Result.retry() // Retry on failure
        }
    }
}

