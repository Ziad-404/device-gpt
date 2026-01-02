package com.teamz.lab.debugger.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.content.edit

object HealthScoreUtils {
    private const val PREFS_NAME = "health_score_prefs"
    private const val KEY_LAST_SCAN_DATE = "last_scan_date"
    private const val KEY_HEALTH_SCORE_HISTORY = "health_score_history"
    private const val KEY_DAILY_STREAK = "daily_streak"
    private const val KEY_BEST_SCORE = "best_score"
    private const val KEY_TOTAL_SCANS = "total_scans"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun calculateDailyHealthScore(context: Context): Int {
        var score = 10 // Start with perfect score

        // Battery Health (0-3 points) - Using existing battery functions
        val batteryInfo = getBatteryChargingInfo(context)
        score -= when {
            batteryInfo.contains("excellent") || batteryInfo.contains("good") || batteryInfo.contains("normal") -> 0
            batteryInfo.contains("fair") -> 1
            batteryInfo.contains("poor") || batteryInfo.contains("bad") -> 2
            batteryInfo.contains("critical") || batteryInfo.contains("overheating") -> 3
            else -> 1 // Default penalty for unknown status
        }

        // Storage Status (0-2 points) - Using existing storage functions
        val storageInfo = getMemoryAndStorageInfo(context)
        score -= when {
            storageInfo.contains("excellent") || storageInfo.contains("good") || storageInfo.contains("available") -> 0
            storageInfo.contains("fair") -> 1
            storageInfo.contains("poor") || storageInfo.contains("full") || storageInfo.contains("low") -> 2
            else -> 1 // Default penalty for unknown status
        }

        // Thermal Status (0-2 points)
        val thermalStatus = getThermalZoneTemperatures(context)
        when {
            thermalStatus.contains("cool") || thermalStatus.contains("normal") -> score -= 0
            thermalStatus.contains("warm") -> score -= 1
            thermalStatus.contains("hot") || thermalStatus.contains("overheating") -> score -= 2
        }

        // RAM Usage (0-1 point)
        val ramUsage = getRamUsage(context)
        if (ramUsage.contains("high") || ramUsage.contains("critical")) {
            score -= 1
        }

        // Security Status (0-2 points)
        val securityInfo = getSecurityInfo(context)
        if (securityInfo.contains("❌") || securityInfo.contains("⚠️")) {
            score -= 2
        }

        // Additional meaningful checks
        
        // Check if device is rooted (major security risk)
        val rootStatus = isDeviceRooted()
        if (rootStatus.contains("Yes") || rootStatus.contains("Rooted")) {
            score -= 2
        }
        
        // Check if USB debugging is enabled (security risk)
        val usbDebugStatus = isUsbDebuggingEnabled(context)
        if (usbDebugStatus.contains("enabled") || usbDebugStatus.contains("Enabled")) {
            score -= 1
        }

        return maxOf(1, score) // Minimum score of 1
    }

    fun saveHealthScore(context: Context, score: Int) {
        val prefs = getPrefs(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        // Get the previous scan date BEFORE updating it
        val previousScanDate = prefs.getString(KEY_LAST_SCAN_DATE, "") ?: ""
        
        // Save today's score
        prefs.edit {
            putInt("score_$today", score)
                .putString(KEY_LAST_SCAN_DATE, today)
        }

        // Update streak with the previous scan date
        updateDailyStreak(context, today, previousScanDate)
        
        // Update best score
        val bestScore = prefs.getInt(KEY_BEST_SCORE, 0)
        if (score > bestScore) {
            prefs.edit { putInt(KEY_BEST_SCORE, score) }
        }

        // Total scans are now calculated from actual history, no need to increment counter
    }

    fun getLastScanDate(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_SCAN_DATE, "") ?: ""
    }

    fun getDailyStreak(context: Context): Int {
        // Recalculate streak based on actual history to ensure accuracy
        return calculateCurrentStreak(context)
    }

    fun getBestScore(context: Context): Int {
        // Recalculate best score from history to ensure accuracy
        return calculateBestScoreFromHistory(context)
    }

    fun getTotalScans(context: Context): Int {
        // Calculate total scans from actual history to ensure accuracy
        return calculateTotalScansFromHistory(context)
    }

    fun getHealthScoreHistory(context: Context, days: Int = 7): List<Pair<String, Int>> {
        val prefs = getPrefs(context)
        val history = mutableListOf<Pair<String, Int>>()
        val calendar = Calendar.getInstance()
        
        for (i in 0 until days) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            val score = prefs.getInt("score_$date", -1)
            if (score != -1) {
                history.add(Pair(date, score))
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return history.reversed()
    }

    /**
     * Calculate total scans from actual scan history
     */
    private fun calculateTotalScansFromHistory(context: Context): Int {
        val prefs = getPrefs(context)
        val allKeys = prefs.all.keys
        val scanKeys = allKeys.filter { it.startsWith("score_") }
        return scanKeys.size
    }

    /**
     * Calculate current streak based on actual scan history
     */
    private fun calculateCurrentStreak(context: Context): Int {
        val history = getHealthScoreHistory(context, 30) // Check last 30 days
        if (history.isEmpty()) return 0
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val calendar = Calendar.getInstance()
        
        var streak = 0
        var currentDate = today
        
        // Check consecutive days starting from today
        while (true) {
            val hasScannedToday = history.any { it.first == currentDate }
            if (hasScannedToday) {
                streak++
                // Move to previous day
                calendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(currentDate) ?: break
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            } else {
                break
            }
        }
        
        return streak
    }

    /**
     * Calculate best score from actual scan history
     */
    private fun calculateBestScoreFromHistory(context: Context): Int {
        val history = getHealthScoreHistory(context, 365) // Check last year
        if (history.isEmpty()) return 0
        
        return history.maxOfOrNull { it.second } ?: 0
    }

    private fun updateDailyStreak(context: Context, today: String, previousScanDate: String) {
        val prefs = getPrefs(context)
        
        if (previousScanDate == today) {
            // Already scanned today, don't update streak
            return
        }
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        
        if (previousScanDate == yesterday) {
            // Consecutive day
            prefs.edit { putInt(KEY_DAILY_STREAK, prefs.getInt(KEY_DAILY_STREAK, 0) + 1) }
        } else {
            // Break in streak
            prefs.edit { putInt(KEY_DAILY_STREAK, 1) }
        }
    }

    fun getHealthScoreMessage(score: Int): String {
        return when {
            score >= 9 -> "Excellent! Your device is in top condition!"
            score >= 7 -> "Good! Your device is performing well."
            score >= 5 -> "Fair. Some improvements needed - see recommendations below."
            else -> "Needs attention. Check recommendations below to improve your device."
        }
    }

    fun getStreakMessage(streak: Int): String {
        return when {
            streak >= 7 -> "🔥 Amazing! ${streak}-day streak! You're a health champion!"
            streak >= 3 -> "⚡ Great! ${streak}-day streak! Keep it up!"
            streak >= 1 -> "📱 Good start! ${streak}-day streak."
            else -> "🚀 Start your daily health check streak today!"
        }
    }

    // Get intelligent improvement suggestions based on actual device data
    fun getImprovementSuggestions(context: Context, score: Int): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Analyze battery status - REAL device data analysis
        val batteryInfo = getBatteryChargingInfo(context)
        when {
            batteryInfo.contains("Overheat") || batteryInfo.contains("Overvoltage") -> {
                suggestions.add("🔥 Your phone is too hot! Take off the case and let it cool down for 10 minutes")
                suggestions.add("⚡ Don't charge while it's hot - this can damage your battery permanently")
                suggestions.add("🌬️ Close all apps and put your phone in a cool place")
            }
            batteryInfo.contains("Failure") || batteryInfo.contains("Dead") -> {
                suggestions.add("💀 Your battery is dying! Time to replace it or get a new phone")
                suggestions.add("🔋 Turn on battery saver mode to make it last longer")
                suggestions.add("📱 Lower your screen brightness to save battery life")
            }
            batteryInfo.contains("Battery Full") -> {
                suggestions.add("🔌 Unplug your phone! Keeping it at 100% all the time kills the battery faster")
                suggestions.add("💡 Try charging only between 20% and 80% for longer battery life")
            }
            batteryInfo.contains("Cold") -> {
                suggestions.add("❄️ Your phone is too cold! Warm it up before charging")
                suggestions.add("🌡️ Don't leave your phone in the car during winter")
            }
        }
        
        // Analyze storage status - REAL device data analysis
        val storageInfo = getMemoryAndStorageInfo(context)
        when {
            storageInfo.contains("GB") -> {
                val storageMatch = Regex("(\\d+\\.?\\d*)\\s*GB").find(storageInfo)
                val availableGB = storageMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                when {
                    availableGB < 2f -> {
                        suggestions.add("💾 Your phone is almost full! Delete old photos and videos you don't need")
                        suggestions.add("📱 Move your pictures to Google Photos to free up space")
                        suggestions.add("🗑️ Go to Settings > Apps and delete apps you never use")
                        suggestions.add("🧹 Clear cache: Settings > Storage > Free up space")
                    }
                    availableGB < 5f -> {
                        suggestions.add("⚠️ Your phone is getting full (${availableGB}GB left). Time to clean up!")
                        suggestions.add("📸 Move your photos to the cloud to save space")
                        suggestions.add("🎵 Delete music you don't listen to anymore")
                    }
                }
            }
            storageInfo.contains("MB") -> {
                val storageMatch = Regex("(\\d+)\\s*MB").find(storageInfo)
                val availableMB = storageMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
                when {
                    availableMB < 500 -> {
                        suggestions.add("🚨 Emergency! Your phone is almost completely full (${availableMB}MB left)!")
                        suggestions.add("📱 Delete apps immediately - start with games you don't play")
                        suggestions.add("📸 Move ALL photos to Google Photos or your computer")
                        suggestions.add("🧹 Clear all cache: Settings > Storage > Free up space")
                    }
                }
            }
        }
        
        // Analyze RAM usage - REAL device data analysis
        val ramUsage = getRamUsage(context)
        val ramMatch = Regex("\\((\\d+)%\\)").find(ramUsage)
        val ramUsagePercent = ramMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
        when {
            ramUsagePercent > 85 -> {
                suggestions.add("🧠 Your phone is struggling! Close all apps and restart it")
                suggestions.add("📱 Don't keep 20 apps open - close the ones you're not using")
                suggestions.add("🔄 Restart your phone daily to keep it running smoothly")
            }
            ramUsagePercent > 70 -> {
                suggestions.add("⚠️ Your phone is getting slow (${ramUsagePercent}% memory used). Close some apps!")
                suggestions.add("📱 Swipe up and close apps you're not using right now")
            }
        }
        
        // Analyze thermal status - REAL device data analysis
        val thermalStatus = getThermalZoneTemperatures(context)
        when {
            thermalStatus.contains("°C") -> {
                val tempMatch = Regex("(\\d+\\.?\\d*)°C").find(thermalStatus)
                val temperature = tempMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                when {
                    temperature > 45f -> {
                        suggestions.add("🌡️ Your phone is burning up (${temperature}°C)! Stop using it immediately")
                        suggestions.add("❄️ Take off the case and put it in front of a fan")
                        suggestions.add("☀️ Don't leave it in the sun or hot car")
                        suggestions.add("📱 Close all apps and let it cool down for 15 minutes")
                    }
                    temperature > 40f -> {
                        suggestions.add("🌡️ Your phone is getting warm (${temperature}°C). Close some apps")
                        suggestions.add("🌬️ Don't use it while charging to keep it cooler")
                    }
                }
            }
        }
        
        // Analyze security status - USER-FRIENDLY recommendations based on actual issues
        val securityInfo = getSecurityInfo(context)
        when {
            securityInfo.contains("❌ Security shield is off") -> {
                suggestions.add("🛡️ Your phone's security system is turned off - this is unusual and risky")
                suggestions.add("📞 Contact your phone's customer support - this should be on by default")
                suggestions.add("⚠️ Be extra careful about which apps you install until this is fixed")
            }
            securityInfo.contains("❌ Storage is not protected") -> {
                suggestions.add("🔐 Your photos and messages aren't protected if someone steals your phone")
                suggestions.add("📱 Go to Settings > Security > Encrypt phone to lock your data")
                suggestions.add("💡 This keeps your private stuff safe even if your phone is lost")
            }
            securityInfo.contains("❌ No admin set") -> {
                suggestions.add("👮 Your phone doesn't recognize you as the owner")
                suggestions.add("📱 Go to Settings > Security > Device admin apps to fix this")
                suggestions.add("🔑 This helps you control your phone's security settings")
            }
            securityInfo.contains("⚠️ Clipboard might be accessed") -> {
                suggestions.add("📎 Your copied text might be seen by other apps (older Android versions)")
                suggestions.add("🔒 Don't copy passwords or credit card numbers on older phones")
                suggestions.add("📱 Update your phone to Android 11+ for automatic protection")
            }
            securityInfo.contains("⚠️ Modified system files found") -> {
                suggestions.add("🚨 Your phone's system has been changed - this could be dangerous")
                suggestions.add("🛡️ Go to Settings > Security > Google Play Protect > Scan device")
                suggestions.add("📞 If you didn't modify your phone, contact customer support")
            }
            securityInfo.contains("🚨 Malware Signatures Detected") -> {
                suggestions.add("☠️ DANGER: Bad apps detected on your phone!")
                suggestions.add("🗑️ Go to Settings > Apps and uninstall suspicious apps immediately")
                suggestions.add("🛡️ Run Google Play Protect: Settings > Security > Google Play Protect")
            }
            securityInfo.contains("👣 Your phone moved while locked") -> {
                suggestions.add("👀 Someone might be touching your phone when you're not around")
                suggestions.add("🔒 Go to Settings > Security > Smart Lock and turn off 'On-body detection'")
                suggestions.add("📱 This prevents your phone from unlocking when it's moved")
            }
            securityInfo.contains("📱") && securityInfo.contains("Permissions:") -> {
                suggestions.add("🔐 Some apps have access to your camera, microphone, and location")
                suggestions.add("📱 Go to Settings > Apps > [app name] > Permissions to review")
                suggestions.add("❌ Turn off permissions that apps don't really need")
            }
            securityInfo.contains("⚠️") -> {
                suggestions.add("🛡️ Update your phone: Settings > System > System update for security fixes")
                suggestions.add("🔒 Set a strong lock screen: Settings > Security > Screen lock")
                suggestions.add("📱 Choose Pattern, PIN, or Password - not just swipe")
            }
        }
        
        // Score-based personalized suggestions
        when {
            score <= 3 -> {
                suggestions.add("🚨 Your phone needs serious help! Follow all the tips above")
                suggestions.add("📱 Consider backing up your data and doing a factory reset")
                suggestions.add("💡 Your phone might be too old - time for an upgrade?")
            }
            score <= 5 -> {
                suggestions.add("⚠️ Your phone has several issues. Focus on battery and storage first")
                suggestions.add("🔄 Restart your phone every day to keep it running better")
                suggestions.add("📱 Don't install too many apps - keep it simple")
            }
            score <= 7 -> {
                suggestions.add("👍 Your phone is doing okay! These tips will make it even better")
                suggestions.add("🔋 Don't charge overnight - unplug when it reaches 80%")
                suggestions.add("📱 Keep your apps updated for better performance")
            }
            score <= 9 -> {
                suggestions.add("🌟 Great job! Your phone is in excellent shape")
                suggestions.add("💡 Keep doing what you're doing - you're taking good care of it!")
                suggestions.add("📱 Check back daily to maintain this great health score")
            }
            else -> {
                suggestions.add("🏆 Perfect! Your phone is in amazing condition")
                suggestions.add("💎 You're a phone care expert! Keep up the great work")
                suggestions.add("📱 Share these tips with friends who need help with their phones")
            }
        }
        
        // Add fun, easy-to-understand maintenance tips
        if (suggestions.size < 6) {
            suggestions.add("💡 Pro tip: Charge your phone like you eat - little and often, not all at once!")
            suggestions.add("🧹 Think of cache like dust - clean it weekly to keep your phone fresh")
            suggestions.add("🔄 Restart your phone weekly - it's like giving it a good night's sleep")
            suggestions.add("📱 Update your apps regularly - it's like getting new features for free!")
            suggestions.add("🔋 Don't let your phone die completely - charge it before it hits 20%")
            suggestions.add("🌡️ Keep your phone cool - hot phones are unhappy phones!")
            suggestions.add("📸 Back up your photos regularly - memories are priceless!")
            suggestions.add("🎮 Don't play games while charging - it's like running while eating!")
        }
        
        return suggestions.take(6) // Return top 6 most helpful suggestions
    }

    // Device Analysis function removed - redundant with Device Info tab
} 