package com.teamz.lab.debugger.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Best Devices Aggregator
 * 
 * Aggregates scores by normalized device model to show top devices per category
 */
object BestDevicesAggregator {
    private const val TAG = "BestDevicesAggregator"
    private val db = FirebaseFirestore.getInstance()
    
    /**
     * Get best devices for a category (top 10)
     */
    suspend fun getBestDevices(category: String): List<BestDeviceEntry> {
        return try {
            // Get top entries from category leaderboard
            val entries = LeaderboardManager.getLeaderboardEntries(category, 100)
            
            // Group by normalized device ID and aggregate
            val byDevice = entries.groupBy { it.normalizedDeviceId }
            
            val bestDevices = byDevice.map { (deviceId, deviceEntries) ->
                val firstEntry = deviceEntries.first()
                val avgScore = deviceEntries.map { it.avgScore }.average()
                val totalUsers = deviceEntries.sumOf { it.userCount }
                val topScore = deviceEntries.maxOfOrNull { it.topScore } ?: 0.0
                val avgDataQuality = deviceEntries.map { it.dataQuality }.average().toInt()
                
                BestDeviceEntry(
                    normalizedDeviceId = deviceId,
                    displayName = firstEntry.displayName,
                    normalizedBrand = firstEntry.normalizedBrand,
                    avgScore = avgScore,
                    userCount = totalUsers,
                    topScore = topScore,
                    dataQuality = avgDataQuality
                )
            }
            
            // Sort by average score and take top 10
            bestDevices.sortedByDescending { it.avgScore }.take(10)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get best devices", e)
            emptyList()
        }
    }
    
    /**
     * Get best devices across all categories
     */
    suspend fun getBestDevicesAllCategories(): Map<String, List<BestDeviceEntry>> {
        val result = mutableMapOf<String, List<BestDeviceEntry>>()
        
        LeaderboardCategory.entries.forEach { category ->
            result[category.id] = getBestDevices(category.id)
        }
        
        return result
    }
}

