package com.teamz.lab.debugger.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * Data Readiness Checker
 * 
 * Ensures device data is fully loaded before uploading to leaderboard
 * This prevents uploading incomplete/zero data that would pollute the database
 */
object DataReadinessChecker {
    private const val TAG = "DataReadinessChecker"
    private const val MAX_WAIT_TIME_MS = 30000L // 30 seconds max wait
    private const val CHECK_INTERVAL_MS = 1000L // Check every 1 second
    private const val MIN_DATA_QUALITY = 2 // Minimum data quality score (out of 5)
    
    /**
     * Wait for device data to be ready before uploading
     * Returns true if data is ready, false if timeout
     */
    suspend fun waitForDataReady(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            // Check if this is first upload (never uploaded before)
            val prefs = context.getSharedPreferences("leaderboard_upload_prefs", Context.MODE_PRIVATE)
            val lastUploadTime = prefs.getLong("last_upload_time", 0L)
            val isFirstUpload = lastUploadTime == 0L
            
            Log.d(TAG, "Checking data readiness (firstUpload: $isFirstUpload)...")
            
            val startTime = System.currentTimeMillis()
            var attempts = 0
            
            while (System.currentTimeMillis() - startTime < MAX_WAIT_TIME_MS) {
                attempts++
                
                // Check if we have minimum required data
                val entry = DataCollectionManager.collectAllUniqueData(context)
                val isReady = isDataReady(entry, isFirstUpload)
                
                if (isReady) {
                    Log.d(TAG, "✅ Data is ready after ${attempts} attempts (${System.currentTimeMillis() - startTime}ms)")
                    return@withContext true
                }
                
                // Log detailed info every 5 attempts
                if (attempts % 5 == 0) {
                    val nonZeroScores = entry.scores.values.count { it > 0.0 }
                    Log.d(TAG, "⏳ Data not ready yet (attempt $attempts): Quality=${entry.dataQuality}/5, NonZeroScores=$nonZeroScores")
                    Log.d(TAG, "  Non-zero categories: ${entry.scores.filter { it.value > 0.0 }.keys.joinToString()}")
                }
                delay(CHECK_INTERVAL_MS)
            }
            
            // Final check
            val entry = DataCollectionManager.collectAllUniqueData(context)
            val isReady = isDataReady(entry, isFirstUpload)
            
            if (isReady) {
                Log.d(TAG, "✅ Data is ready after timeout check")
                return@withContext true
            } else {
                val nonZeroScores = entry.scores.values.count { it > 0.0 }
                Log.w(TAG, "⚠️ Data not ready after ${MAX_WAIT_TIME_MS}ms timeout.")
                Log.w(TAG, "  Quality: ${entry.dataQuality}/5, NonZeroScores: $nonZeroScores, FirstUpload: $isFirstUpload")
                Log.w(TAG, "  Scores: ${entry.scores.entries.joinToString { "${it.key}=${it.value}" }}")
                return@withContext false
            }
        }
    }
    
    /**
     * Check if data meets minimum quality requirements
     * For decision-making data, we need:
     * - Minimum data quality score (at least 1/5 for first upload, 2/5 for subsequent)
     * - At least some non-zero scores (not all zeros)
     * - Recent timestamp
     */
    private fun isDataReady(entry: LeaderboardEntry, isFirstUpload: Boolean = false): Boolean {
        // RELAXED: For first upload, allow lower quality (1/5 instead of 2/5)
        // This allows new devices to upload even if they haven't run all tests yet
        val minQuality = if (isFirstUpload) 1 else MIN_DATA_QUALITY
        
        if (entry.dataQuality < minQuality) {
            Log.d(TAG, "Data quality too low: ${entry.dataQuality}/5 (minimum: $minQuality, firstUpload: $isFirstUpload)")
            return false
        }
        
        // Check if we have at least some non-zero scores
        val nonZeroScores = entry.scores.values.count { it > 0.0 }
        if (nonZeroScores == 0) {
            Log.d(TAG, "All scores are zero - data not ready")
            Log.d(TAG, "  Scores breakdown: ${entry.scores.entries.joinToString { "${it.key}=${it.value}" }}")
            return false
        }
        
        // RELAXED: For first upload, allow just 1 category (instead of 2)
        // This allows devices with only power data or only health score to upload
        val minCategories = if (isFirstUpload) 1 else 2
        if (nonZeroScores < minCategories) {
            Log.d(TAG, "Too few categories with data: $nonZeroScores (minimum: $minCategories, firstUpload: $isFirstUpload)")
            Log.d(TAG, "  Non-zero categories: ${entry.scores.filter { it.value > 0.0 }.keys.joinToString()}")
            return false
        }
        
        // Check timestamp is recent (within last hour)
        val ageMs = System.currentTimeMillis() - entry.timestamp
        if (ageMs > 3600000L) { // 1 hour
            Log.d(TAG, "Data timestamp too old: ${ageMs}ms")
            return false
        }
        
        Log.d(TAG, "✅ Data is ready: Quality=${entry.dataQuality}/5, NonZeroScores=$nonZeroScores, Age=${ageMs}ms, FirstUpload=$isFirstUpload")
        return true
    }
    
    /**
     * Check if data is ready synchronously (for immediate checks)
     */
    suspend fun isDataReadyNow(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            // Check if this is first upload
            val prefs = context.getSharedPreferences("leaderboard_upload_prefs", Context.MODE_PRIVATE)
            val lastUploadTime = prefs.getLong("last_upload_time", 0L)
            val isFirstUpload = lastUploadTime == 0L
            
            val entry = DataCollectionManager.collectAllUniqueData(context)
            isDataReady(entry, isFirstUpload)
        }
    }
}

