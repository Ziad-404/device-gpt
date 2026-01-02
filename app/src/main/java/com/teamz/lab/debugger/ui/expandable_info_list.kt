package com.teamz.lab.debugger.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import com.teamz.lab.debugger.utils.AIIcon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.graphics.Color
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.teamz.lab.debugger.BuildConfig
import com.teamz.lab.debugger.utils.AnalyticsEvent
import com.teamz.lab.debugger.utils.AnalyticsUtils
import com.teamz.lab.debugger.utils.RemoteConfigUtils.shouldShowNativeAds
import com.teamz.lab.debugger.utils.copyToClipboard
import com.teamz.lab.debugger.utils.AdRevenueOptimizer
import com.teamz.lab.debugger.ui.theme.DesignSystemColors
import com.teamz.lab.debugger.utils.AdConfig

// Sealed class to represent list items (info or ad)
private sealed class ListItem {
    data class InfoItem(val index: Int, val key: String, val value: String) : ListItem()
    data class AdItem(val index: Int, val ad: NativeAd) : ListItem()
}

/**
 * Generates a context-specific AI prompt for a device info item
 * 
 * Delegates to AIPromptGenerator for centralized prompt management
 */
fun generateItemPrompt(itemTitle: String, itemContent: String, appName: String, promptMode: PromptMode): String {
    // Delegate to centralized prompt generator
    return com.teamz.lab.debugger.utils.AIPromptGenerator.generateItemPrompt(
        itemTitle = itemTitle,
        itemContent = itemContent,
        appName = appName,
        promptMode = promptMode
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableInfoList(
    infoList: List<Pair<String, String>>, 
    activity: Activity,
    onAIClick: (() -> Unit)? = null,
    onItemAIClick: ((String, String) -> Unit)? = null
) {

    val adLoader = rememberAdLoader(activity)
    var searchQuery by remember { mutableStateOf("") }
    val shownAdHashes = remember {
        mutableStateMapOf(
            "list" to mutableSetOf(), "expand" to mutableSetOf<Int>()
        )
    }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Use derivedStateOf for filtering - only recalculates when searchQuery or infoList changes
    // Note: derivedStateOf must be created outside remember to track changes properly
    val filteredInfo = derivedStateOf {
        if (searchQuery.isEmpty()) {
            infoList
        } else {
            infoList.filter { (title, detail) ->
                title.contains(searchQuery, ignoreCase = true) || detail.contains(
                    searchQuery,
                    ignoreCase = true
                )
            }
        }
    }
    
    // Create index map once for O(1) lookup instead of O(n) indexOfFirst
    val originalIndexMap = remember(infoList) {
        infoList.mapIndexedNotNull { index, (key, _) -> key to index }.toMap()
    }
    
    // Create flattened list with info items and ads - optimized with index map
    val flattenedList = remember(filteredInfo.value, originalIndexMap) {
        val list = mutableListOf<ListItem>()
        val filtered = filteredInfo.value
        filtered.forEachIndexed { filteredIndex, (key, value) ->
            // Use O(1) map lookup instead of O(n) indexOfFirst
            val actualIndex = originalIndexMap[key] ?: filteredIndex
            
            // Add ad before item if it's a 5th item (except first)
            if (actualIndex % 5 == 0 && actualIndex != 0 && actualIndex < infoList.size) {
                val positionId = "device_info_list_$actualIndex"
                val listAd = NativeAdManager.getAdForPosition(positionId)
                listAd?.let {
                    list.add(ListItem.AdItem(actualIndex, it))
                }
            }
            
            // Add the info item
            list.add(ListItem.InfoItem(actualIndex, key, value))
        }
        list
    }
    
    // Single expanded state map instead of per-item state (reduces recompositions)
    val expandedItems = remember { mutableStateMapOf<Int, Boolean>() }
    val inlineAds = remember { mutableStateMapOf<Int, NativeAd?>() }
    
    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            .navigationBarsPadding(),
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        // Search bar at top with optional AI button
        item(key = "search_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        AnalyticsUtils.logEvent(AnalyticsEvent.SearchUsed, mapOf("query" to it))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // List items with virtualization - only visible items are composed
        itemsIndexed(
            items = flattenedList,
            key = { index, item ->
                when (item) {
                    is ListItem.AdItem -> "ad_${item.index}"
                    is ListItem.InfoItem -> "info_${item.index}_${item.key}"
                }
            }
        ) { _, item ->
            when (item) {
                is ListItem.AdItem -> {
                    // Render ad
                    Spacer(modifier = Modifier.height(12.dp))
                    AdMobNativeAdCard(nativeAd = item.ad)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                is ListItem.InfoItem -> {
                    // Render info item
                    val actualIndex = item.index
                    val key = item.key
                    val value = item.value
                    val expanded = expandedItems[actualIndex] ?: false
                    val inlineAd = inlineAds[actualIndex]
                    
                    // 🔄 Dynamically load an ad when expanded, but only if a new one exists
                    // AdMob Best Practice: Use different ad for expanded view vs list ads
                    // Use key to prevent unnecessary LaunchedEffect restarts
                    LaunchedEffect(key1 = expanded, key2 = actualIndex) {
                        if (expanded && inlineAd == null) {
                            if (shouldShowNativeAds()) {
                                // Use position-specific ad to ensure different ad in expanded view
                                val positionId = "device_info_expanded_$actualIndex"
                                val newAd = NativeAdManager.getAdForPosition(positionId)
                                newAd?.let {
                                    inlineAds[actualIndex] = it
                                }
                            }
                        }
                    }

                    val showExpandedAd = expanded && inlineAd != null
                    AppCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable {
                                    expandedItems[actualIndex] = !expanded
                                    AnalyticsUtils.logEvent(
                                        if (!expanded) AnalyticsEvent.InfoExpanded else AnalyticsEvent.InfoCollapsed,
                                        mapOf("title" to key)
                                    )
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onItemAIClick != null) {
                                    IconButton(
                                        onClick = {
                                            onItemAIClick(key, value)
                                            AnalyticsUtils.logEvent(AnalyticsEvent.FabAIClicked, mapOf(
                                                "source" to "device_info_item",
                                                "item_title" to key
                                            ))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = AIIcon.icon,
                                            contentDescription = "Get AI insights about $key",
                                            tint = AIIcon.color(),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                if (value.length > 50) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (expanded) "Collapse" else "Expand",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            overflow = if (value.length > 50) TextOverflow.Ellipsis else TextOverflow.Clip,
                            modifier = Modifier
                                .combinedClickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        expandedItems[actualIndex] = !expanded
                                        AnalyticsUtils.logEvent(
                                            if (!expanded) AnalyticsEvent.InfoExpanded else AnalyticsEvent.InfoCollapsed,
                                            mapOf("title" to key)
                                        )
                                    },
                                    onLongClick = {
                                        copyToClipboard(
                                            context = activity,
                                            title = key,
                                            body = value
                                        )
                                    }
                                )
                        )
                        if (showExpandedAd) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimatedVisibility(
                                visible = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                AdMobNativeAdCard(
                                    bottomPadding = 0,
                                    nativeAd = inlineAd!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    bottomPadding: Int = 16,
    borderColor: Color=MaterialTheme.colorScheme.outline,
    colors: CardColors = CardDefaults.cardColors( containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(bottom = bottomPadding.dp),
        shape = RoundedCornerShape(12.dp),
        colors = colors,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(), content = content
        )
    }
}

@Composable
fun rememberAdLoader(activity: Activity): AdLoader {
    val coroutineScope = rememberCoroutineScope()
    var adLoaderRef: AdLoader? by remember { mutableStateOf(null) }
    
    val adLoader = remember {
        val adUnitId = AdConfig.getNativeAdUnitId()
        AdLoader.Builder(activity, adUnitId)
            .forNativeAd { nativeAd ->
                if (!activity.isDestroyed) {
                    NativeAdManager.nativeAds.add(nativeAd)
                    AnalyticsUtils.logEvent(AnalyticsEvent.AdLoaded)
                    // Track revenue for native ads
                    nativeAd.setOnPaidEventListener { adValue ->
                        AdRevenueOptimizer.trackAdRevenue(
                            activity,
                            adUnitId,
                            "native",
                            adValue
                        )
                    }
                } else {
                    nativeAd.destroy()
                }
            }.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    val errorCode = adError.code
                    val errorMessage = adError.message ?: "unknown"
                    println("❌ Failed to load ad: $errorMessage")
                    
                    AnalyticsUtils.logEvent(
                        AnalyticsEvent.AdFailed,
                        mapOf(
                            "error_code" to errorCode,
                            "error_message" to errorMessage,
                            "ad_type" to "native"
                        )
                    )
                    
                    // Retry loading with exponential backoff (only for retryable errors)
                    // Error codes: 0=INVALID_REQUEST, 2=INVALID_AD_SIZE, 8=INVALID_APP_ID
                    val nonRetryableErrors = listOf(0, 2, 8)
                    
                    // Only retry if we don't have enough ads AND can make request
                    val targetCount = NativeAdManager.getTargetAdCount()
                    val currentCount = NativeAdManager.nativeAds.filterNotNull().size
                    
                    if (errorCode !in nonRetryableErrors && 
                        currentCount < targetCount &&
                        NativeAdManager.canMakeRequest()) {
                        // Retry after delay with throttling
                        adLoaderRef?.let { loader ->
                            coroutineScope.launch {
                                delay(5000) // 5 second delay (increased from 3s)
                                if (!activity.isDestroyed && 
                                    NativeAdManager.nativeAds.filterNotNull().size < targetCount &&
                                    NativeAdManager.canMakeRequest()) {
                                    NativeAdManager.recordRequest()
                                    loader.loadAd(AdRequest.Builder().build())
                                }
                            }
                        }
                    }
                }

                override fun onAdClicked() {
                    AnalyticsUtils.logEvent(AnalyticsEvent.AdClicked)
                    AdRevenueOptimizer.trackAdClick(activity, "native")
                }
            }).withNativeAdOptions(NativeAdOptions.Builder().build()).build()
            .also { adLoaderRef = it }
    }

    LaunchedEffect(Unit) {
        // CRITICAL: Use atomic initialization to prevent race condition
        // Only ONE composable should initialize ads, even if multiple call rememberAdLoader()
        val targetAdCount = NativeAdManager.getTargetAdCount()
        val currentCount = NativeAdManager.nativeAds.filterNotNull().size
        
        // Atomic check-and-set: only first caller gets true
        if (NativeAdManager.tryMarkInitialized() && 
            currentCount < targetAdCount && 
            !NativeAdManager.isCurrentlyLoading() &&
            NativeAdManager.canMakeRequest()) {
            
            NativeAdManager.setLoading(true)
            
            // Load only 3 ads (enough for rotation in "every 5 items" display)
            val adsToLoad = targetAdCount - currentCount
            repeat(adsToLoad) { index ->
                kotlinx.coroutines.delay(index * 2000L) // Stagger by 2 seconds (was 500ms)
                if (!activity.isDestroyed && 
                    NativeAdManager.nativeAds.filterNotNull().size < targetAdCount &&
                    NativeAdManager.canMakeRequest()) {
                    NativeAdManager.recordRequest()
                    adLoader.loadAd(AdRequest.Builder().build())
                }
            }
            
            // Reset loading flag after a delay to allow for async loading
            coroutineScope.launch {
                delay(adsToLoad * 2000L + 1000L)
                NativeAdManager.setLoading(false)
            }
        }
    }

    return adLoader
}

