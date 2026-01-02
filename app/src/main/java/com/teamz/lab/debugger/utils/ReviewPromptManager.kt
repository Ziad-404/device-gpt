package com.teamz.lab.debugger.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.teamz.lab.debugger.services.userHasAlreadyReviewed
import com.teamz.lab.debugger.services.setAlreadyReviewed
import com.teamz.lab.debugger.utils.LeaderboardManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

/**
 * Review Prompt Manager
 * 
 * Manages automatic review prompts using Google Play In-App Review API.
 * Follows Google's best practices:
 * - Shows after positive user experience (not on first launch)
 * - Respects frequency limits (Google allows max 3 prompts per year)
 * - Shows at appropriate times (after app opens, with delay)
 * - Only shows if user hasn't reviewed yet
 */
object ReviewPromptManager {
    private const val TAG = "ReviewPromptManager"
    private const val PREFS_NAME = "review_prompt_prefs"
    
    // Firebase collection
    private const val FIRESTORE_COLLECTION = "user_reviews"
    private const val FIRESTORE_FIELD_HAS_REVIEWED = "has_reviewed"
    private const val FIRESTORE_FIELD_REVIEWED_DATE = "reviewed_date"
    
    // Configuration
    private const val MIN_APP_OPENS_BEFORE_PROMPT = 3 // Show after 3 app opens
    private const val MIN_DAYS_BETWEEN_PROMPTS = 30 // Don't show more than once per month
    private const val DELAY_BEFORE_SHOWING_MS = 2000L // Wait 2 seconds after app opens
    
    // Preference keys
    private const val KEY_APP_OPEN_COUNT = "app_open_count"
    private const val KEY_LAST_PROMPT_TIME = "last_prompt_time"
    private const val KEY_FIRST_LAUNCH_DATE = "first_launch_date"
    private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Track app open and show review prompt if conditions are met
     * 
     * @param activity The activity context
     * @param isColdStart Whether this is a cold start (first launch after app was closed)
     */
    fun trackAppOpenAndMaybeShowReview(activity: Activity, isColdStart: Boolean) {
        val context = activity.applicationContext
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check if user already reviewed (local check first - fast)
        if (context.userHasAlreadyReviewed()) {
            Log.d(TAG, "trackAppOpenAndMaybeShowReview() - User already reviewed (local), skipping")
            // Still sync in background to keep Firebase updated
            CoroutineScope(Dispatchers.IO).launch {
                syncReviewStatusFromFirebase(context)
            }
            return
        }
        
        // Sync review status from Firebase (if user is logged in) - non-blocking
        // This ensures review status is synced across devices
        // Note: We check local first (fast), then sync in background
        CoroutineScope(Dispatchers.IO).launch {
            syncReviewStatusFromFirebase(context)
        }
        
        // Track first launch date
        val firstLaunchDate = prefs.getLong(KEY_FIRST_LAUNCH_DATE, 0L)
        if (firstLaunchDate == 0L) {
            prefs.edit {
                putLong(KEY_FIRST_LAUNCH_DATE, System.currentTimeMillis())
            }
            Log.d(TAG, "trackAppOpenAndMaybeShowReview() - First launch tracked, not showing review yet")
            return // Don't show on first launch
        }
        
        // Increment app open count
        val appOpenCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0) + 1
        prefs.edit {
            putInt(KEY_APP_OPEN_COUNT, appOpenCount)
        }
        Log.d(TAG, "trackAppOpenAndMaybeShowReview() - App open count: $appOpenCount")
        
        // Check if we should show review prompt
        if (shouldShowReviewPrompt(context)) {
            // Show review after a delay (so it doesn't interrupt app opening)
            CoroutineScope(Dispatchers.Main).launch {
                delay(DELAY_BEFORE_SHOWING_MS)
                
                // Double-check conditions before showing (user might have reviewed in the meantime)
                // Quick sync before showing (with timeout to avoid blocking too long)
                try {
                    // Try to sync quickly (500ms timeout) before showing
                    withTimeout(500) {
                        syncReviewStatusFromFirebase(context)
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Log.d(TAG, "trackAppOpenAndMaybeShowReview() - Sync timeout, proceeding with local check")
                } catch (e: Exception) {
                    Log.w(TAG, "trackAppOpenAndMaybeShowReview() - Sync error, proceeding with local check", e)
                }
                
                // Final check before showing
                if (!context.userHasAlreadyReviewed() && shouldShowReviewPrompt(context)) {
                    Log.d(TAG, "trackAppOpenAndMaybeShowReview() - ✅ Showing review prompt")
                    showReviewPrompt(activity)
                } else {
                    Log.d(TAG, "trackAppOpenAndMaybeShowReview() - ⚠️ Conditions changed, not showing review")
                }
            }
        } else {
            Log.d(TAG, "trackAppOpenAndMaybeShowReview() - ⚠️ Not showing review (conditions not met)")
        }
    }
    
    /**
     * Check if we should show review prompt based on:
     * - App open count (minimum 3 opens)
     * - Time since last prompt (minimum 30 days)
     * - User hasn't reviewed yet
     */
    private fun shouldShowReviewPrompt(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Check app open count
        val appOpenCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        if (appOpenCount < MIN_APP_OPENS_BEFORE_PROMPT) {
            Log.d(TAG, "shouldShowReviewPrompt() - App open count ($appOpenCount) < minimum ($MIN_APP_OPENS_BEFORE_PROMPT)")
            return false
        }
        
        // Check time since last prompt
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        if (lastPromptTime > 0) {
            val timeSinceLastPrompt = System.currentTimeMillis() - lastPromptTime
            val daysSinceLastPrompt = timeSinceLastPrompt / (24 * 60 * 60 * 1000)
            
            if (daysSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) {
                Log.d(TAG, "shouldShowReviewPrompt() - Only $daysSinceLastPrompt days since last prompt (need $MIN_DAYS_BETWEEN_PROMPTS)")
                return false
            }
        }
        
        // Check if user already reviewed (local check - fast)
        if (context.userHasAlreadyReviewed()) {
            Log.d(TAG, "shouldShowReviewPrompt() - User already reviewed")
            return false
        }
        
        Log.d(TAG, "shouldShowReviewPrompt() - ✅ All conditions met, should show review")
        return true
    }
    
    /**
     * Show Google Play In-App Review prompt
     * Uses the official Google Play In-App Review API
     */
    private fun showReviewPrompt(activity: Activity) {
        try {
            val context = activity.applicationContext
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Update last prompt time
            prefs.edit {
                putLong(KEY_LAST_PROMPT_TIME, System.currentTimeMillis())
            }
            
            // Track analytics
            AnalyticsUtils.logEvent(AnalyticsEvent.ReviewRequested)
            
            // Use Google Play In-App Review API
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Review flow is available, show it
                    val reviewInfo = task.result
                    manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener {
                        Log.d(TAG, "showReviewPrompt() - ✅ Review flow completed")
                        // Note: We don't automatically set "already reviewed" here because:
                        // 1. User might dismiss without rating
                        // 2. Google Play API handles frequency limits automatically
                        // 3. We'll sync from Firebase if user reviewed on another device
                    }
                } else {
                    // Review flow not available (e.g., device not supported, too many requests)
                    Log.w(TAG, "showReviewPrompt() - ⚠️ Review flow not available: ${task.exception?.message}")
                    // Fallback: Open Play Store page (but don't mark as reviewed)
                    AnalyticsUtils.logEvent(AnalyticsEvent.ReviewOpenedPlayStore)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "showReviewPrompt() - ❌ Error showing review prompt", e)
            ErrorHandler.handleError(e, context = "ReviewPromptManager.showReviewPrompt")
        }
    }
    
    /**
     * Check if user has reviewed (checks local only - fast)
     * Firebase sync happens in background, so we rely on local status
     * which gets updated when sync completes
     */
    private fun hasUserReviewed(context: Context): Boolean {
        // Check local only (fast, non-blocking)
        // Firebase sync happens in background and updates local status
        return context.userHasAlreadyReviewed()
    }
    
    /**
     * Sync review status from Firebase
     * This ensures if user reviewed on Device A, Device B will know about it
     */
    private suspend fun syncReviewStatusFromFirebase(context: Context) {
        val currentUser = auth.currentUser ?: return
        
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)
            val timeSinceSync = System.currentTimeMillis() - lastSyncTime
            val oneHour = 60 * 60 * 1000L
            
            // Only sync if it's been more than 1 hour (avoid too frequent syncs)
            if (timeSinceSync < oneHour) {
                Log.d(TAG, "syncReviewStatusFromFirebase() - Recently synced, skipping")
                return
            }
            
            Log.d(TAG, "syncReviewStatusFromFirebase() - Syncing review status for user: ${currentUser.uid}")
            
            val reviewDoc = db.collection(FIRESTORE_COLLECTION)
                .document(currentUser.uid)
                .get()
                .await()
            
            if (reviewDoc.exists()) {
                val hasReviewed = reviewDoc.getBoolean(FIRESTORE_FIELD_HAS_REVIEWED) ?: false
                
                if (hasReviewed) {
                    // User reviewed on another device - update local status
                    Log.d(TAG, "syncReviewStatusFromFirebase() - ✅ User reviewed on another device, updating local status")
                    context.setAlreadyReviewed(true)
                }
                
                // Update last sync time
                prefs.edit {
                    putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                }
            } else {
                // No review data in Firebase yet - check if local says reviewed and upload it
                if (context.userHasAlreadyReviewed()) {
                    Log.d(TAG, "syncReviewStatusFromFirebase() - Local says reviewed, uploading to Firebase")
                    saveReviewStatusToFirebase(context, true)
                }
                
                // Update last sync time
                prefs.edit {
                    putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncReviewStatusFromFirebase() - ❌ Error syncing review status", e)
            // Don't throw - this is a background sync, shouldn't block app
        }
    }
    
    /**
     * Save review status to Firebase
     * This ensures review status is synced across all user's devices
     * Can be called from external code (e.g., when user manually reviews)
     */
    suspend fun saveReviewStatusToFirebase(context: Context, hasReviewed: Boolean) {
        val currentUser = auth.currentUser ?: return
        
        try {
            Log.d(TAG, "saveReviewStatusToFirebase() - Saving review status: $hasReviewed for user: ${currentUser.uid}")
            
            val reviewData = hashMapOf(
                FIRESTORE_FIELD_HAS_REVIEWED to hasReviewed,
                FIRESTORE_FIELD_REVIEWED_DATE to if (hasReviewed) System.currentTimeMillis() else null,
                "userId" to currentUser.uid,
                "lastUpdated" to System.currentTimeMillis()
            )
            
            db.collection(FIRESTORE_COLLECTION)
                .document(currentUser.uid)
                .set(reviewData)
                .await()
            
            Log.d(TAG, "saveReviewStatusToFirebase() - ✅ Review status saved to Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "saveReviewStatusToFirebase() - ❌ Error saving review status", e)
            // Don't throw - this is a background operation
        }
    }
    
    /**
     * Reset review prompt data (for testing purposes)
     */
    @Suppress("unused")
    fun resetReviewPromptData(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            clear()
        }
        Log.d(TAG, "resetReviewPromptData() - Review prompt data reset")
    }
    
    /**
     * Get current review prompt state (for debugging)
     */
    @Suppress("unused")
    fun getReviewPromptState(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val appOpenCount = prefs.getInt(KEY_APP_OPEN_COUNT, 0)
        val lastPromptTime = prefs.getLong(KEY_LAST_PROMPT_TIME, 0L)
        val firstLaunchDate = prefs.getLong(KEY_FIRST_LAUNCH_DATE, 0L)
        val hasReviewed = context.userHasAlreadyReviewed()
        
        val daysSinceLastPrompt = if (lastPromptTime > 0) {
            (System.currentTimeMillis() - lastPromptTime) / (24 * 60 * 60 * 1000)
        } else {
            -1
        }
        
        return """
            ReviewPromptManager State:
            - App Open Count: $appOpenCount (need $MIN_APP_OPENS_BEFORE_PROMPT)
            - Has Reviewed: $hasReviewed
            - Last Prompt: ${if (lastPromptTime > 0) "$daysSinceLastPrompt days ago" else "Never"}
            - First Launch: ${if (firstLaunchDate > 0) "${(System.currentTimeMillis() - firstLaunchDate) / (24 * 60 * 60 * 1000)} days ago" else "Not tracked"}
            - Should Show: ${shouldShowReviewPrompt(context)}
        """.trimIndent()
    }
}

