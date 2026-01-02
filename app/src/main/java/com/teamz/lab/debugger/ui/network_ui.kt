package com.teamz.lab.debugger.ui

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.teamz.lab.debugger.utils.calculateInternetHealthScore
import com.teamz.lab.debugger.utils.string
import com.teamz.lab.debugger.R
import com.teamz.lab.debugger.utils.checkISPStreamingServers
import com.teamz.lab.debugger.utils.checkInternetPrivacyAndSurveillance
import com.teamz.lab.debugger.utils.getCaptivePortalStatus
import com.teamz.lab.debugger.utils.getDnsServers
import com.teamz.lab.debugger.utils.getGatewayAddress
import com.teamz.lab.debugger.utils.getIPv4v6Support
import com.teamz.lab.debugger.utils.getISPDetails
import com.teamz.lab.debugger.utils.getInternetUptime
import com.teamz.lab.debugger.utils.getJitter
import com.teamz.lab.debugger.utils.getLocalIPAddress
import com.teamz.lab.debugger.utils.getMobileSpeed
import com.teamz.lab.debugger.utils.getMtu
import com.teamz.lab.debugger.utils.getNetworkDownloadSpeed
import com.teamz.lab.debugger.utils.getNetworkType
import com.teamz.lab.debugger.utils.getNetworkUploadSpeed
import com.teamz.lab.debugger.utils.getNetworkUsageStatsThrottled
import com.teamz.lab.debugger.utils.getPacketLoss
import com.teamz.lab.debugger.utils.getPublicIPAddressFromIPInfo
import com.teamz.lab.debugger.utils.getWiFiInformation
import com.teamz.lab.debugger.utils.pingPopularServers
import com.teamz.lab.debugger.utils.testNetworkLatency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun NetworkInfoSection(
    activity: Activity, 
    onShareClick: (String) -> Unit,
    onItemAIClick: ((String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork?.let { activeNetwork ->
        connectivityManager.getNetworkCapabilities(activeNetwork)
    }

    val coroutineScope = rememberCoroutineScope()
    val loadingText = context.string(R.string.loading)

    var ipAddress by remember { mutableStateOf(loadingText) }
    var mobileSpeed by remember { mutableStateOf(loadingText) }
    var ipvSupport by remember { mutableStateOf(loadingText) }
    var packetLoss by remember { mutableStateOf(loadingText) }
    var jitter by remember { mutableStateOf(loadingText) }
    var captivePortalStatus by remember { mutableStateOf(loadingText) }
    var ispDetails by remember { mutableStateOf(loadingText) }
    var ispStreamingServers by remember { mutableStateOf(loadingText) }
    var govSurveillance by remember { mutableStateOf(loadingText) }
    var downloadSpeed by remember { mutableStateOf(loadingText) }
    var uploadSpeed by remember { mutableStateOf(loadingText) }
    var gatewayAddress by remember { mutableStateOf(loadingText) }
    var dnsServers by remember { mutableStateOf(loadingText) }
    var mtuSize by remember { mutableStateOf(loadingText) }
    var internetUptime by remember { mutableStateOf(loadingText) }
    var localIpAddress
            by remember { mutableStateOf(loadingText) }
    var wifiInfo by remember { mutableStateOf(loadingText) }
    var pingResults by remember { mutableStateOf(loadingText) }
    var networkUsageStats by remember { mutableStateOf(loadingText) }
    var healthScore by remember { mutableStateOf(loadingText) }


    fun refreshNetworkInfo() {
        coroutineScope.launch(Dispatchers.Main) {
            val mobileSpeedDeferred = async(Dispatchers.IO) { getMobileSpeed(networkCapabilities) }
            val ipvSupportDeferred = async(Dispatchers.IO) { getIPv4v6Support() }
            val packetLossDeferred = async(Dispatchers.IO) { getPacketLoss() }
            val jitterDeferred = async(Dispatchers.IO) { getJitter() }
            val captivePortalStatusDeferred =
                async(Dispatchers.IO) { getCaptivePortalStatus(context) }
            val gatewayAddressDeferred = async(Dispatchers.IO) { getGatewayAddress() }
            val dnsServersDeferred = async(Dispatchers.IO) { getDnsServers(context) }
            val mtuSizeDeferred = async(Dispatchers.IO) { getMtu() }
            val internetUptimeDeferred = async(Dispatchers.IO) { getInternetUptime() }
            val localIpAddressDeferred = async(Dispatchers.IO) { getLocalIPAddress(context) }
            val ipAddressDeferred = async(Dispatchers.IO) { getPublicIPAddressFromIPInfo() }
            val govSurveillanceDeferred =
                async(Dispatchers.IO) { checkInternetPrivacyAndSurveillance() }
            val downloadSpeedDeferred = async(Dispatchers.IO) { getNetworkDownloadSpeed() }
            val uploadSpeedDeferred = async(Dispatchers.IO) { getNetworkUploadSpeed() }
            val wifiInfoDeferred = async(Dispatchers.IO) { getWiFiInformation(context) }
            val ispDetailsDeferred = async(Dispatchers.IO) { getISPDetails() }
            val streamingServersDeferred = async(Dispatchers.IO) { checkISPStreamingServers() }

            val packetLossText = packetLossDeferred.await()
            val jitterText = jitterDeferred.await()
            val downloadSpeedText = downloadSpeedDeferred.await()
            val uploadSpeedText = uploadSpeedDeferred.await()

            val latencyMsDeferred = async(Dispatchers.IO) {
                val regex = Regex("time=(\\d+(\\.\\d+)?)")
                val match = regex.find(testNetworkLatency())
                match?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
            }

            val jitterMsDeferred = async(Dispatchers.IO) {
                val value = Regex("([\\d.]+)").find(jitterText)?.groups?.get(1)?.value?.toDoubleOrNull()
                value ?: 0.0
            }

            val packetLossPercentDeferred = async(Dispatchers.IO) {
                Regex("(\\d+)%").find(packetLossText)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            }

            val downloadMbpsDeferred = async(Dispatchers.IO) {
                Regex("([\\d.]+)").find(downloadSpeedText)?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
            }

            val uploadMbpsDeferred = async(Dispatchers.IO) {
                Regex("([\\d.]+)").find(uploadSpeedText)?.groups?.get(1)?.value?.toDoubleOrNull() ?: 0.0
            }


            coroutineScope.launch {
                val resultBuilder = StringBuilder()
                getNetworkUsageStatsThrottled(context) { usage ->
                    resultBuilder.append("${usage.period}\nWi-Fi: ${usage.wifiUsage} | Mobile: ${usage.mobileUsage}\n\n")
                }
                networkUsageStats = resultBuilder.toString().trim()
            }

            val pingDeferred = async(Dispatchers.IO) { pingPopularServers() }

            // Update each variable as soon as the result arrives (non-blocking)
            launch { mobileSpeed = mobileSpeedDeferred.await() }
            launch { ipvSupport = ipvSupportDeferred.await() }
            launch { packetLoss = packetLossDeferred.await() }
            launch { jitter = jitterDeferred.await() }
            launch { captivePortalStatus = captivePortalStatusDeferred.await() }
            launch { gatewayAddress = gatewayAddressDeferred.await() }
            launch { dnsServers = dnsServersDeferred.await() }
            launch { mtuSize = mtuSizeDeferred.await() }
            launch { internetUptime = internetUptimeDeferred.await() }
            launch { localIpAddress = localIpAddressDeferred.await() }
            launch { ipAddress = ipAddressDeferred.await() }
            launch { govSurveillance = govSurveillanceDeferred.await() }
            launch { downloadSpeed = downloadSpeedDeferred.await() }
            launch { uploadSpeed = uploadSpeedDeferred.await() }
            launch { wifiInfo = wifiInfoDeferred.await() }
            launch { ispDetails = ispDetailsDeferred.await() }
            launch { ispStreamingServers = streamingServersDeferred.await() }
            launch { pingResults = pingDeferred.await() }
            launch {
                healthScore = calculateInternetHealthScore(
                    latencyMsDeferred.await(),
                    jitterMsDeferred.await(),
                    packetLossPercentDeferred.await(),
                    downloadMbpsDeferred.await(),
                    uploadMbpsDeferred.await()
                )
            }

        }
    }


    LaunchedEffect(Unit) {
        refreshNetworkInfo()
    }

    val networkInfo = listOf(
        "Network Usage Breakdown" to networkUsageStats,
        "ISP Details" to ispDetails, // ✅ ISP Name & ASN
        "ISP Streaming/CDN Servers" to ispStreamingServers,
        "Government & ISP Surveillance Test" to govSurveillance,
        "Internet Health Score" to healthScore,
        "Mobile Data Speed" to mobileSpeed,
        "Download Speed" to downloadSpeed,
        "Upload Speed" to uploadSpeed,
        "Network Packet Loss" to packetLoss,
        "Connection Stability (Jitter)" to jitter,
        "Response Speed (Latency)" to testNetworkLatency(),
        "Ping Test to Popular Servers" to pingResults,
        "Internet Protocol Support" to ipvSupport,
        "Local IP Addresses" to localIpAddress,
        "Public IP Address" to ipAddress,
        "Router IP Address (Gateway)" to gatewayAddress,
        "Complete Wi-Fi Information" to wifiInfo,
        "Connected Network" to getNetworkType(context),
        "Requires Login to Use Internet (Captive Portal)" to captivePortalStatus, // Non-Tech: Redirect on connect, Tech: Captive Portal
        "DNS Servers" to dnsServers, // ✅ Shows DNS Details
        "Data Packet Limit (MTU)" to mtuSize, // Tech: MTU, Non-Tech: Packet Size Limit
        "Internet Active Time" to internetUptime, // Non-Tech: How long the internet has been active
    )

    // Check if all network data is fully loaded
    // All items should have data (not equal to loadingText)
    val isFullyLoaded = remember(
        networkUsageStats, ispDetails, ispStreamingServers, govSurveillance,
        healthScore, mobileSpeed, downloadSpeed, uploadSpeed, packetLoss,
        jitter, pingResults, ipvSupport, localIpAddress, ipAddress,
        gatewayAddress, wifiInfo, captivePortalStatus, dnsServers,
        mtuSize, internetUptime
    ) {
        networkInfo.all { (_, content) -> 
            content.isNotEmpty() && content != loadingText
        }
    }

    // Generate share content only when ALL data is fully loaded
    val shareContent = if (isFullyLoaded) {
        networkInfo.joinToString("\n\n") { (title, content) ->
            "$title\n$content"
        }
    } else {
        loadingText
    }

    // Only call onShareClick when ALL data is fully loaded
    LaunchedEffect(isFullyLoaded, shareContent) {
        if (isFullyLoaded && shareContent != loadingText) {
            onShareClick(shareContent)
        }
    }

    ExpandableInfoList(
        infoList = networkInfo, 
        activity = activity,
        onItemAIClick = if (isFullyLoaded) onItemAIClick else null
    )
}
