package com.teamz.lab.debugger.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

/**
 * FPS Data Cache
 * 
 * Caches FPS data collected on main thread so it can be used in background uploads
 * This ensures we don't lose valuable FPS data just because upload happens on background thread
 */
object FpsDataCache {
    private const val TAG = "FpsDataCache"
    private const val PREFS_NAME = "fps_data_cache"
    private const val KEY_FPS = "cached_fps"
    private const val KEY_FRAME_DROP_RATE = "cached_frame_drop_rate"
    private const val KEY_TIMESTAMP = "cached_timestamp"
    private const val KEY_FPS_DATA_STRING = "cached_fps_data_string"
    
    // Cache validity: 5 minutes (FPS can change quickly)
    private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L
    
    /**
     * Save FPS data to cache (called when on main thread)
     */
    fun saveFpsData(context: Context, fps: Int, frameDropRate: Double, fpsDataString: String) {
        try {
            getPrefs(context).edit {
                putInt(KEY_FPS, fps)
                putFloat(KEY_FRAME_DROP_RATE, frameDropRate.toFloat())
                putString(KEY_FPS_DATA_STRING, fpsDataString)
                putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            }
            // Log.d(TAG, "✅ FPS data cached: FPS=$fps, DropRate=$frameDropRate%") // Disabled: Too verbose
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache FPS data", e)
        }
    }
    
    /**
     * Get cached FPS data (can be called from any thread)
     * Returns null if cache is expired or doesn't exist
     */
    fun getCachedFpsData(context: Context): CachedFpsData? {
        return try {
            val prefs = getPrefs(context)
            val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
            
            // Check if cache exists
            if (timestamp == 0L) {
                Log.d(TAG, "No cached FPS data found")
                return null
            }
            
            // Check if cache is still valid (not expired)
            val age = System.currentTimeMillis() - timestamp
            if (age > CACHE_VALIDITY_MS) {
                Log.d(TAG, "Cached FPS data expired (age: ${age}ms, max: ${CACHE_VALIDITY_MS}ms)")
                return null
            }
            
            val fps = prefs.getInt(KEY_FPS, 0)
            val frameDropRate = prefs.getFloat(KEY_FRAME_DROP_RATE, 0f).toDouble()
            val fpsDataString = prefs.getString(KEY_FPS_DATA_STRING, "") ?: ""
            
            // Log.d(TAG, "✅ Using cached FPS data: FPS=$fps, DropRate=$frameDropRate% (age: ${age}ms)") // Disabled: Too verbose
            
            CachedFpsData(
                fps = fps,
                frameDropRate = frameDropRate,
                fpsDataString = fpsDataString,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get cached FPS data", e)
            null
        }
    }
    
    /**
     * Check if we have valid cached FPS data
     */
    fun hasValidCache(context: Context): Boolean {
        return getCachedFpsData(context) != null
    }
    
    /**
     * Clear cached FPS data
     */
    fun clearCache(context: Context) {
        try {
            getPrefs(context).edit {
                clear()
            }
            Log.d(TAG, "FPS cache cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear FPS cache", e)
        }
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Cached FPS data structure
     */
    data class CachedFpsData(
        val fps: Int,
        val frameDropRate: Double,
        val fpsDataString: String,
        val timestamp: Long
    )
}

