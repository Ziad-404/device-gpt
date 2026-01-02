package com.teamz.lab.debugger.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import java.util.*

/**
 * ReferralManager - Handles referral tracking and deep linking for viral growth
 * Tracks who referred whom and rewards users for sharing
 */
object ReferralManager {
    private const val PREFS_NAME = "referral_prefs"
    private const val KEY_REFERRAL_CODE = "user_referral_code"
    private const val KEY_REFERRED_BY = "referred_by_code"
    private const val KEY_REFERRAL_COUNT = "referral_count"
    private const val KEY_FIRST_OPEN_TIME = "first_open_time"
    private const val KEY_IS_REFERRER = "is_referrer"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Generate or retrieve user's unique referral code
     */
    fun getOrCreateReferralCode(context: Context): String {
        val prefs = getPrefs(context)
        var code = prefs.getString(KEY_REFERRAL_CODE, null)
        
        if (code == null) {
            // Generate unique code: USER + random 6 digits
            code = "USER${Random().nextInt(900000) + 100000}"
            prefs.edit {
                putString(KEY_REFERRAL_CODE, code)
                putLong(KEY_FIRST_OPEN_TIME, System.currentTimeMillis())
            }
            
            // Track first open
            AnalyticsUtils.logEvent(
                AnalyticsEvent.AppOpened,
                mapOf("is_first_open" to true, "referral_code" to code)
            )
        }
        
        return code
    }
    
    /**
     * Check if user was referred by someone and track it
     */
    fun checkReferral(context: Context, intent: Intent?) {
        val prefs = getPrefs(context)
        
        // Check if already processed
        if (prefs.contains(KEY_REFERRED_BY)) {
            return
        }
        
        // Check deep link or intent data
        val referralCode = intent?.data?.getQueryParameter("ref") 
            ?: intent?.getStringExtra("referral_code")
            ?: intent?.getStringExtra("ref")
        
        if (referralCode != null && referralCode.isNotEmpty()) {
            val userCode = getOrCreateReferralCode(context)
            
            // Don't count self-referrals
            if (referralCode != userCode) {
                prefs.edit {
                    putString(KEY_REFERRED_BY, referralCode)
                }
                
                // Track referral installation
                AnalyticsUtils.logEvent(
                    AnalyticsEvent.ReferralInstalled,
                    mapOf(
                        "referral_code" to referralCode,
                        "user_code" to userCode,
                        "source" to (intent?.data?.scheme ?: "unknown")
                    )
                )
                
                // Track for referrer (if they're in the system)
                trackReferralForReferrer(context, referralCode)
            }
        }
    }
    
    /**
     * Track that someone used this user's referral code
     */
    private fun trackReferralForReferrer(context: Context, referrerCode: String) {
        // This would typically be tracked server-side, but we log it for analytics
        AnalyticsUtils.logEvent(
            AnalyticsEvent.ReferralShared,
            mapOf(
                "referrer_code" to referrerCode,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
    
    /**
     * Get referral link for sharing
     */
    fun getReferralLink(context: Context): String {
        val code = getOrCreateReferralCode(context)
        val packageName = context.packageName
        return "https://play.google.com/store/apps/details?id=$packageName&referrer=utm_source%3Dreferral%26utm_medium%3Dshare%26utm_campaign%3D$code"
    }
    
    /**
     * Get short referral link (for SMS/WhatsApp)
     */
    fun getShortReferralLink(context: Context): String {
        val code = getOrCreateReferralCode(context)
        // You can use a URL shortener service here
        return "https://play.google.com/store/apps/details?id=${context.packageName}&ref=$code"
    }
    
    /**
     * Get referral count (how many people this user referred)
     */
    fun getReferralCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_REFERRAL_COUNT, 0)
    }
    
    /**
     * Increment referral count (called when someone installs via referral)
     */
    fun incrementReferralCount(context: Context) {
        val prefs = getPrefs(context)
        val count = prefs.getInt(KEY_REFERRAL_COUNT, 0) + 1
        prefs.edit {
            putInt(KEY_REFERRAL_COUNT, count)
        }
        
        // Track milestone achievements
        when (count) {
            1 -> AnalyticsUtils.logEvent(
                AnalyticsEvent.AchievementUnlocked,
                mapOf("achievement" to "first_referral", "count" to count)
            )
            5 -> AnalyticsUtils.logEvent(
                AnalyticsEvent.AchievementUnlocked,
                mapOf("achievement" to "referral_milestone_5", "count" to count)
            )
            10 -> AnalyticsUtils.logEvent(
                AnalyticsEvent.AchievementUnlocked,
                mapOf("achievement" to "referral_milestone_10", "count" to count)
            )
        }
    }
    
    /**
     * Check if user was referred by someone
     */
    fun wasReferred(context: Context): Boolean {
        return getPrefs(context).contains(KEY_REFERRED_BY)
    }
    
    /**
     * Get the code of the person who referred this user
     */
    fun getReferredByCode(context: Context): String? {
        return getPrefs(context).getString(KEY_REFERRED_BY, null)
    }
    
    /**
     * Share referral link via intent
     */
    fun shareReferralLink(context: Context, shareText: String = "") {
        val referralLink = getReferralLink(context)
        val code = getOrCreateReferralCode(context)
        
        val defaultText = """
            🔍 Check out this amazing device health checker app!
            
            📱 Get detailed insights about your phone's performance, battery, storage, and security.
            
            Use my referral code: $code
            
            Download now: $referralLink
            
            #PhoneHealth #DeviceChecker #TechTools
        """.trimIndent()
        
        val finalText = if (shareText.isNotEmpty()) shareText else defaultText
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, finalText)
            putExtra(Intent.EXTRA_SUBJECT, "Check out this amazing device health app!")
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
        
        // Track referral sharing
        AnalyticsUtils.logEvent(
            AnalyticsEvent.ReferralShared,
            mapOf(
                "referral_code" to code,
                "method" to "generic_share"
            )
        )
    }
}

