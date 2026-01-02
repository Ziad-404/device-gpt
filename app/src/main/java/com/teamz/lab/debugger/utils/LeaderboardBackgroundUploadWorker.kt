package com.teamz.lab.debugger.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.util.Log

/**
 * Background worker for periodic leaderboard data upload
 * Runs daily to ensure data is uploaded even if user doesn't open the app
 */
class LeaderboardBackgroundUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Background upload worker started")
            
            // Upload data using the same logic as app start
            LeaderboardDataUpload.uploadDailyBackground(applicationContext)
            
            Log.d(TAG, "Background upload worker completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background upload worker failed", e)
            // Retry on failure
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "LeaderboardUploadWorker"
        
        /**
         * Schedule daily background upload
         */
        fun scheduleDailyUpload(context: Context) {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val uploadRequest = androidx.work.PeriodicWorkRequestBuilder<LeaderboardBackgroundUploadWorker>(
                24, java.util.concurrent.TimeUnit.HOURS,
                1, java.util.concurrent.TimeUnit.HOURS // Flex interval
            )
                .setConstraints(constraints)
                .addTag("leaderboard_upload")
                .build()
            
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "leaderboard_daily_upload",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    uploadRequest
                )
            
            Log.d(TAG, "Daily background upload scheduled")
        }
    }
}

