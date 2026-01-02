package com.teamz.lab.debugger.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.teamz.lab.debugger.BuildConfig
import com.teamz.lab.debugger.utils.AdRevenueOptimizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

object RewardedAdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private val adUnitId: String = AdConfig.getRewardedAdUnitId()

    fun loadAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (isLoading || rewardedAd != null) return
        isLoading = true

        if (RemoteConfigUtils.shouldShowRewardedAds()) {
            val request = AdRequest.Builder().build()
            
            RewardedAd.load(
                context,
                adUnitId,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                        isLoading = false
                        android.util.Log.e("RewardedAdManager", "Failed to load rewarded ad: ${adError.message}")
                        
                        AnalyticsUtils.logEvent(
                            AnalyticsEvent.AdFailed,
                            mapOf(
                                "error_code" to adError.code,
                                "error_message" to (adError.message ?: "unknown"),
                                "ad_type" to "rewarded"
                            )
                        )
                        
                        // Retry with exponential backoff for retryable errors
                        val nonRetryableErrors = listOf(0, 2, 8) // INVALID_REQUEST, INVALID_AD_SIZE, INVALID_APP_ID
                        if (adError.code !in nonRetryableErrors) {
                            CoroutineScope(Dispatchers.IO).launch {
                                delay(3000)
                                if (!((context as? Activity)?.isDestroyed ?: false)) {
                                    loadAd(context, onLoaded)
                                }
                            }
                        }
                        
                        onLoaded?.invoke()
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoading = false
                        
                        // Set revenue tracking listener
                        ad.setOnPaidEventListener(
                            AdRevenueOptimizer.createRevenueListener(
                                context,
                                adUnitId,
                                "rewarded"
                            )
                        )
                        
                        AnalyticsUtils.logEvent(AnalyticsEvent.AdLoaded)
                        onLoaded?.invoke()
                    }
                }
            )
        } else {
            isLoading = false
            onLoaded?.invoke()
        }
    }

    fun showAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        // Preload next ad immediately for better revenue
        if (rewardedAd == null && !isLoading) {
            loadAd(activity)
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadAd(activity) // Preload next ad
                    onAdDismissed()
                    AnalyticsUtils.logEvent(AnalyticsEvent.AppOpenAdDismissed)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadAd(activity)
                    onAdFailed()
                    android.util.Log.e("RewardedAdManager", "Failed to show rewarded ad: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    AnalyticsUtils.logEvent(AnalyticsEvent.AppFullScreenAdShown)
                }

                override fun onAdClicked() {
                    AnalyticsUtils.logEvent(AnalyticsEvent.AppFullScreenAdClicked)
                    AdRevenueOptimizer.trackAdClick(activity, "rewarded")
                }
            }

            if (RemoteConfigUtils.shouldShowRewardedAds()) {
                ad.show(
                    activity,
                    OnUserEarnedRewardListener { rewardItem ->
                        // User earned reward
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        android.util.Log.d("RewardedAdManager", "User earned reward: $rewardAmount $rewardType")
                        
                        AnalyticsUtils.logEvent(
                            AnalyticsEvent.AdRewardEarned,
                            mapOf(
                                "reward_amount" to rewardAmount,
                                "reward_type" to rewardType
                            )
                        )
                        
                        onRewardEarned()
                    }
                )
            } else {
                onAdFailed()
            }
        } else {
            // Ad not loaded, try to load and show later
            loadAd(activity) {
                if (rewardedAd != null) {
                    showAd(activity, onRewardEarned, onAdDismissed, onAdFailed)
                } else {
                    onAdFailed()
                }
            }
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
    fun isLoading(): Boolean = isLoading
}

