package com.teamz.lab.debugger.ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.teamz.lab.debugger.ui.theme.DesignSystemColors


@Composable
fun AdMobNativeAdCard(nativeAd: NativeAd, bottomPadding: Int = 16) {
    NativeAdView(ad = nativeAd, adContent = { ad, composeView ->
        AppCard(
            borderColor = DesignSystemColors.NeonGreen,
            bottomPadding = bottomPadding,
            colors = CardDefaults.cardColors(
                containerColor = DesignSystemColors.NeonGreen,
                contentColor = DesignSystemColors.NeonGreen,
            )
        ) {
            Text(
                "Sponsored", style = MaterialTheme.typography.bodySmall,
                color = DesignSystemColors.Dark,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                ad.headline ?: "",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = DesignSystemColors.Dark,
            )

            ad.body?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    it,
                    color = DesignSystemColors.Dark,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ad.icon?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it.drawable),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                ad.callToAction?.let {
                    Button(
                        colors = ButtonColors(
                            containerColor = DesignSystemColors.Dark,
                            contentColor = DesignSystemColors.White,
                            disabledContainerColor = DesignSystemColors.Dark,
                            disabledContentColor = DesignSystemColors.White
                        ),
                        onClick = { composeView.performClick() }) {
                        Text(it, color = DesignSystemColors.White)
                    }
                }
            }
        }
    })
}

@Composable
fun NativeAdView(
    ad: NativeAd,
    adContent: @Composable (ad: NativeAd, contentView: View) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    AndroidView(
        factory = { context ->
            val adView = NativeAdView(context).apply {
                id = adViewId
            }

            val contentView = ComposeView(context).apply {
                id = contentViewId
            }

            // AdChoices view required by AdMob
            val adChoicesView = AdChoicesView(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.END
                )
            }

            adView.addView(contentView)
            adView.addView(adChoicesView)

            // Attach required views
            adView.adChoicesView = adChoicesView
            adView.headlineView = contentView
            adView.bodyView = contentView
            adView.iconView = contentView
            adView.callToActionView = contentView

            adView.setNativeAd(ad)

            contentView.setContent {
                adContent(ad, adView)
            }

            adView
        },
        update = { view ->

            val adView = view.findViewById<NativeAdView>(adViewId)
            val contentView = view.findViewById<ComposeView>(contentViewId)

            adView.setNativeAd(ad)
            adView.callToActionView = contentView
            contentView.setContent { adContent(ad, contentView) }
        }
    )
}

object NativeAdManager {
    var nativeAds = mutableStateListOf<NativeAd?>()
    @Volatile private var isLoading = false
    @Volatile private var hasInitialized = false
    private var lastRequestTime = 0L
    private const val MIN_REQUEST_INTERVAL_MS = 5000L // 5 seconds between requests
    private var currentRotationIndex = 0 // For ad rotation
    private val initializationLock = Any() // Lock for thread-safe initialization

    fun clear() {
        nativeAds.forEach { it?.destroy() }
        nativeAds.clear()
        synchronized(this) {
            isLoading = false
            hasInitialized = false
            currentRotationIndex = 0
        }
    }
    
    fun setLoading(loading: Boolean) {
        synchronized(this) {
            isLoading = loading
        }
    }
    
    fun isCurrentlyLoading(): Boolean = synchronized(this) { isLoading }
    
    /**
     * Atomically check and mark as initialized
     * Returns true if this was the first call (should initialize)
     * Returns false if already initialized (should skip)
     */
    fun tryMarkInitialized(): Boolean {
        return synchronized(initializationLock) {
            if (!hasInitialized) {
                hasInitialized = true
                true // First to initialize
            } else {
                false // Already initialized
            }
        }
    }
    
    fun hasBeenInitialized(): Boolean = synchronized(this) { hasInitialized }
    
    /**
     * Get next ad in rotation for "every 5 items" display
     * This ensures different ads are shown, maximizing revenue
     * 
     * NOTE: This increments rotation index - use carefully to avoid frequent changes
     * For stable ad display, prefer pre-calculating ad assignments
     */
    fun getNextAdForRotation(): NativeAd? {
        val validAds = nativeAds.filterNotNull()
        if (validAds.isEmpty()) return null
        
        synchronized(this) {
            val ad = validAds[currentRotationIndex % validAds.size]
            currentRotationIndex++
            return ad
        }
    }
    
    /**
     * Get ad at specific index (for stable assignment)
     * This doesn't increment rotation index, so it's safe for recomposition
     */
    fun getAdAtIndex(index: Int): NativeAd? {
        val validAds = nativeAds.filterNotNull()
        if (validAds.isEmpty()) return null
        return validAds[index % validAds.size]
    }
    
    /**
     * Get a unique ad for a specific position (for different ads in different places)
     * AdMob Best Practice: Show different ads in different positions to maximize revenue
     * 
     * @param positionId Unique identifier for the position (e.g., "top_banner", "list_0", "list_5")
     * @return Different ad for each position, or fallback to same ad if not enough ads available
     */
    fun getAdForPosition(positionId: String): NativeAd? {
        val validAds = nativeAds.filterNotNull()
        if (validAds.isEmpty()) return null
        
        // Use position ID hash to assign different ads to different positions
        // This ensures same position always gets same ad (stable), but different positions get different ads
        val positionHash = positionId.hashCode()
        val adIndex = kotlin.math.abs(positionHash) % validAds.size
        
        return validAds[adIndex]
    }
    
    /**
     * Check if we can make a new ad request (throttling)
     */
    fun canMakeRequest(): Boolean {
        val now = System.currentTimeMillis()
        return (now - lastRequestTime) >= MIN_REQUEST_INTERVAL_MS
    }
    
    fun recordRequest() {
        lastRequestTime = System.currentTimeMillis()
    }
    
    /**
     * Get target ad count based on usage
     * AdMob Best Practice: Load multiple ads to show different ads in different positions
     * This prevents ad fatigue and maximizes revenue
     * 
     * For leaderboard:
     * - 1 top banner ad
     * - Up to 10-20 list ads (every 5 entries = 2-4 ads visible)
     * - Total: 5-6 ads for good diversity
     */
    fun getTargetAdCount(): Int = 6 // Increased for better ad diversity
}



