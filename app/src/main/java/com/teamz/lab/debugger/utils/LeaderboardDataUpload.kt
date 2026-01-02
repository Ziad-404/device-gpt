package com.teamz.lab.debugger.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Leaderboard Data Upload Manager
 * 
 * Handles automatic data collection and upload triggers
 * Reuses existing data collection methods
 */
object LeaderboardDataUpload {
    private const val TAG = "LeaderboardDataUpload"
    private const val PREFS_NAME = "leaderboard_upload_prefs"
    private const val KEY_LAST_UPLOAD_TIME = "last_upload_time"
    private const val MIN_UPLOAD_INTERVAL_MS = 3600000L // 1 hour minimum between uploads
    private const val FIRST_UPLOAD_DELAY_MS = 5000L // 5 seconds after app start for first upload
    
    /**
     * Upload data after health scan
     * All uploads run on IO thread to avoid UI blocking
     */
    fun uploadAfterHealthScan(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    // Quick check if data is ready (don't wait long for user-initiated actions)
                    val isDataReady = DataReadinessChecker.isDataReadyNow(context)
                    if (!isDataReady) {
                        Log.w(TAG, "Skipping upload after health scan - data not ready")
                        return@launch
                    }
                    
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Data uploaded after health scan")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload after health scan", e)
            }
        }
    }
    
    /**
     * Upload data after power measurement
     * All uploads run on IO thread to avoid UI blocking
     */
    fun uploadAfterPowerMeasurement(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    val isDataReady = DataReadinessChecker.isDataReadyNow(context)
                    if (!isDataReady) {
                        Log.w(TAG, "Skipping upload after power measurement - data not ready")
                        return@launch
                    }
                    
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Data uploaded after power measurement")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload after power measurement", e)
            }
        }
    }
    
    /**
     * Upload data after CPU test
     * All uploads run on IO thread to avoid UI blocking
     */
    fun uploadAfterCpuTest(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    val isDataReady = DataReadinessChecker.isDataReadyNow(context)
                    if (!isDataReady) {
                        Log.w(TAG, "Skipping upload after CPU test - data not ready")
                        return@launch
                    }
                    
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Data uploaded after CPU test")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload after CPU test", e)
            }
        }
    }
    
    /**
     * Upload data after camera test
     * All uploads run on IO thread to avoid UI blocking
     */
    fun uploadAfterCameraTest(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    val isDataReady = DataReadinessChecker.isDataReadyNow(context)
                    if (!isDataReady) {
                        Log.w(TAG, "Skipping upload after camera test - data not ready")
                        return@launch
                    }
                    
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Data uploaded after camera test")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload after camera test", e)
            }
        }
    }
    
    /**
     * Upload data after display test
     * All uploads run on IO thread to avoid UI blocking
     */
    fun uploadAfterDisplayTest(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    val isDataReady = DataReadinessChecker.isDataReadyNow(context)
                    if (!isDataReady) {
                        Log.w(TAG, "Skipping upload after display test - data not ready")
                        return@launch
                    }
                    
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Data uploaded after display test")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload after display test", e)
            }
        }
    }
    
    /**
     * Automatic upload on app start
     * Uploads data when app starts if enough time has passed since last upload
     * WAITS for device data to be fully loaded before uploading (data purity)
     */
    fun uploadOnAppStart(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Wait a bit for Firebase Auth to initialize
                kotlinx.coroutines.delay(FIRST_UPLOAD_DELAY_MS)
                
                if (shouldUpload(context)) {
                    Log.d(TAG, "Starting automatic upload on app start...")
                    
                    // CRITICAL: Wait for device data to be fully loaded before uploading
                    // This ensures we don't upload incomplete/zero data that would pollute the database
                    val isDataReady = DataReadinessChecker.waitForDataReady(context)
                    
                    if (isDataReady) {
                        val entry = DataCollectionManager.collectAllUniqueData(context)
                        val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                        if (success) {
                            saveLastUploadTime(context)
                            Log.d(TAG, "✅ Automatic upload on app start completed")
                        } else {
                            Log.w(TAG, "⚠️ Automatic upload on app start failed")
                        }
                    } else {
                        Log.w(TAG, "⚠️ Skipping upload - device data not ready yet (incomplete/zero data)")
                    }
                } else {
                    val lastUpload = getLastUploadTime(context)
                    val timeSinceLastUpload = System.currentTimeMillis() - lastUpload
                    val hoursSince = timeSinceLastUpload / 3600000.0
                    Log.d(TAG, "Skipping upload - last upload was ${String.format("%.1f", hoursSince)} hours ago (minimum: 1 hour)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed automatic upload on app start", e)
            }
        }
    }
    
    /**
     * Daily background upload (if enabled)
     */
    fun uploadDailyBackground(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (shouldUpload(context)) {
                    val entry = DataCollectionManager.collectAllUniqueData(context)
                    val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                    if (success) {
                        saveLastUploadTime(context)
                        Log.d(TAG, "Daily background upload completed")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed daily background upload", e)
            }
        }
    }
    
    /**
     * Force upload (ignores throttling) - use sparingly
     * FIXED: Now runs entirely on background thread to avoid UI blocking
     * FPS data collection is handled gracefully (skipped if not on main thread)
     */
    fun forceUpload(context: Context) {
        // Run entirely on IO thread to avoid blocking UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Force uploading leaderboard data...")
                
                // Wait for data to be ready (data purity)
                val isDataReady = DataReadinessChecker.waitForDataReady(context)
                
                if (!isDataReady) {
                    Log.w(TAG, "⚠️ Force upload skipped - device data not ready (incomplete/zero data)")
                    return@launch
                }
                
                // Collect data (FPS will be skipped if not on main thread, but that's OK)
                val entry = DataCollectionManager.collectAllUniqueData(context)
                
                // Upload on IO thread (network operations)
                val success = LeaderboardManager.uploadLeaderboardEntry(context, entry)
                
                if (success) {
                    saveLastUploadTime(context)
                    Log.d(TAG, "✅ Force upload completed")
                } else {
                    Log.w(TAG, "⚠️ Force upload failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed force upload", e)
            }
        }
    }
    
    /**
     * Check if we should upload (throttling)
     * Returns true if:
     * - Never uploaded before (lastUpload == 0), OR
     * - Enough time has passed since last upload (>= 1 hour)
     */
    private fun shouldUpload(context: Context): Boolean {
        val lastUpload = getLastUploadTime(context)
        if (lastUpload == 0L) {
            // Never uploaded before - allow upload
            return true
        }
        val now = System.currentTimeMillis()
        return (now - lastUpload) >= MIN_UPLOAD_INTERVAL_MS
    }
    
    private fun getLastUploadTime(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_UPLOAD_TIME, 0L)
    }
    
    private fun saveLastUploadTime(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_UPLOAD_TIME, System.currentTimeMillis())
            .apply()
    }
}

