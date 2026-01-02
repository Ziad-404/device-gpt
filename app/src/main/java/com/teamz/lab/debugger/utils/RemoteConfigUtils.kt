package com.teamz.lab.debugger.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.ktx.Firebase
import com.teamz.lab.debugger.BuildConfig

object RemoteConfigUtils {
    private val remoteConfig: FirebaseRemoteConfig
        get() = FirebaseRemoteConfig.getInstance()

    fun init() {
        android.util.Log.d("RemoteConfigUtils", "init() - Initializing RemoteConfig...")
        
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "show_interstitial_ads" to true,
                "show_banner_ads" to true,
                "show_app_open_ads" to true,
                "show_native_ads" to true,
                "show_rewarded_ads" to true,
                "interstitial_ad_interval" to 60L, // Minimum seconds between full-screen ads (global throttling, applies to all)
                // Leaderboard configuration
                "enable_leaderboard" to true,
                "leaderboard_tab_position" to -1L, // -1 = last tab, 0 = first tab
                "leaderboard_ad_frequency" to 5L, // Native ad every 5 entries
                "show_leaderboard_interstitial_ads" to true,
                "leaderboard_data_retention_days" to -1L, // -1 = keep forever, 0+ = days before removal
                "leaderboard_data_retention_reminder_days" to 5L // Days before removal to show reminder (only if retention > 0)
            )
        )
        
        android.util.Log.d("RemoteConfigUtils", "init() - Defaults set, fetching and activating...")
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.d("RemoteConfigUtils", "init() - ✅ RemoteConfig activated successfully")
                android.util.Log.d("RemoteConfigUtils", "init() - show_app_open_ads: ${remoteConfig.getBoolean("show_app_open_ads")}")
            } else {
                android.util.Log.e("RemoteConfigUtils", "init() - ❌ RemoteConfig activation failed: ${task.exception?.message}")
            }
        }
    }

    fun shouldShowInterstitialAds(): Boolean {
        // Disable video ads (interstitial) in debug mode
        if (BuildConfig.DEBUG) {
            return false
        }
        return remoteConfig.getBoolean("show_interstitial_ads")
    }
    
    fun shouldShowBannerAds(): Boolean = remoteConfig.getBoolean("show_banner_ads")
    
    fun shouldShowAppOpenAds(): Boolean {
        val shouldShow = remoteConfig.getBoolean("show_app_open_ads")
        android.util.Log.d("RemoteConfigUtils", "shouldShowAppOpenAds() - Returning: $shouldShow")
        return shouldShow
    }
    
    fun shouldShowNativeAds(): Boolean = remoteConfig.getBoolean("show_native_ads")
    
    fun shouldShowRewardedAds(): Boolean {
        // Disable video ads (rewarded) in debug mode
        if (BuildConfig.DEBUG) {
            return false
        }
        return remoteConfig.getBoolean("show_rewarded_ads")
    }
    
    /**
     * Get minimum interval between interstitial ads (in seconds)
     * This is the ONLY variable that controls full-screen ad frequency globally
     * Applies to all full-screen ads across the entire app
     * Default: 60 seconds (prevents ad spam and improves UX)
     */
    fun getInterstitialAdInterval(): Long {
        val interval = remoteConfig.getLong("interstitial_ad_interval")
        return if (interval == 0L) 60L else interval // Default: 60 seconds
    }
    
    // CSV View Ad Controls - REMOVED
    // CSV view ads now use global show_interstitial_ads flag (handled by InterstitialAdManager)
    
    // Leaderboard configuration
    fun isLeaderboardEnabled(): Boolean = remoteConfig.getBoolean("enable_leaderboard")
    
    fun getLeaderboardTabPosition(): Int {
        // 0 = first tab, -1 = last tab (default)
        val position = remoteConfig.getLong("leaderboard_tab_position")
        return if (position == 0L) 0 else -1 // Default to last tab
    }
    
    fun getLeaderboardAdFrequency(): Int {
        val frequency = remoteConfig.getLong("leaderboard_ad_frequency")
        return if (frequency == 0L) 5 else frequency.toInt() // Default: every 5 entries
    }
    
    fun shouldShowLeaderboardInterstitialAds(): Boolean {
        return remoteConfig.getBoolean("show_leaderboard_interstitial_ads")
    }
    
    fun getLeaderboardDataRetentionDays(): Long {
        val days = remoteConfig.getLong("leaderboard_data_retention_days")
        // -1 means keep forever, 0 means use default (365 days), positive number = days
        return when {
            days == -1L -> -1L // Keep forever
            days == 0L -> 365L // Default: 1 year if not specified
            else -> days
        }
    }
    
    fun getLeaderboardDataRetentionReminderDays(): Long {
        val days = remoteConfig.getLong("leaderboard_data_retention_reminder_days")
        return if (days == 0L) 5L else days // Default: 5 days before removal
    }
} 