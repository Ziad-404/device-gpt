package com.teamz.lab.debugger.utils

import android.app.Activity
import com.teamz.lab.debugger.utils.AnalyticsUtils
import com.teamz.lab.debugger.utils.AnalyticsEvent

/**
 * Centralized AI click handler that shows fullscreen ads before opening AI dialogs
 * 
 * This ensures all AI clicks throughout the app follow AdMob restrictions:
 * - Shows interstitial ad if available and allowed
 * - Respects cooldown/throttling
 * - Handles analytics tracking
 * - Executes callback after ad is dismissed or skipped
 * 
 * Usage:
 * ```kotlin
 * AIClickHandler.handleAIClick(activity, source = "health_score") {
 *     // Open AI dialog or perform action
 *     showAIDialog = true
 * }
 * ```
 */
object AIClickHandler {
    
    /**
     * Handle AI icon click with centralized ad logic
     * 
     * @param activity The activity context
     * @param source Source identifier for analytics (e.g., "health_score", "power_app", "device_info_item")
     * @param itemTitle Optional item title for item-specific AI clicks
     * @param onAIClick Callback to execute after ad is shown/dismissed or skipped
     */
    fun handleAIClick(
        activity: Activity,
        source: String,
        itemTitle: String? = null,
        onAIClick: () -> Unit
    ) {
        // Log analytics
        val analyticsParams = if (itemTitle != null) {
            mapOf<String, Any?>(
                "source" to source,
                "item_title" to itemTitle
            )
        } else {
            mapOf<String, Any?>(
                "source" to source
            )
        }
        AnalyticsUtils.logEvent(AnalyticsEvent.FabAIClicked, analyticsParams)
        
        // Show ad if available, then execute callback
        // InterstitialAdManager handles all checks centrally:
        // - RemoteConfig enable/disable flag
        // - Global time-based throttling
        // - Ad loading and showing
        InterstitialAdManager.showAdIfAvailable(activity) {
            // Ad closed or skipped - execute AI action
            onAIClick()
        }
    }
}

