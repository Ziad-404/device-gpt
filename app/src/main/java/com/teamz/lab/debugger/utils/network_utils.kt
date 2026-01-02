package com.teamz.lab.debugger.utils

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import android.text.format.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow


fun getNetworkType(context: Context): String {
    val networkCapabilities = networkCapabilities(context)
    return when {
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "📶 Wi-Fi (Wireless)"
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "📡 Mobile Data (SIM)"
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> "🛡️ VPN Active"
        networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "🔌 Ethernet (Cable)"
        else -> "❌ No Connection"
    }
}

private fun networkCapabilities(context: Context): NetworkCapabilities? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities =
        connectivityManager.activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
    return networkCapabilities
}

fun getNetworkDownloadSpeed(): String {
    return try {
        val start = System.nanoTime()
        val url = URL("https://speed.cloudflare.com/__down?bytes=10000000")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()
        connection.inputStream.readBytes()
        val end = System.nanoTime()
        val duration = (end - start) / 1_000_000_000.0
        val speed = 10 / duration
        "${"%.2f".format(speed)} Mbps"
    } catch (e: Exception) {
        handleError(e)
        "Speed Test Failed ❌"
    }
}

fun getNetworkUploadSpeed(): String {
    return try {
        val url = URL("https://httpbin.org/post")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true

        val dataSizeMB = 2 // Upload 2MB dummy data
        val dummyData = ByteArray(dataSizeMB * 1024 * 1024) { 'A'.code.toByte() }

        val start = System.nanoTime()

        connection.outputStream.use { it.write(dummyData) }

        connection.inputStream.bufferedReader().readText()

        val end = System.nanoTime()
        val duration = (end - start) / 1_000_000_000.0
        val speed = dataSizeMB / duration
        "${"%.2f".format(speed)} Mbps"
    } catch (e: Exception) {
        handleError(e)
        "Upload Test Failed ❌"
    }
}


fun testNetworkLatency(): String {
    return try {
        val process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8")
        val output = process.inputStream.bufferedReader().readText()

        val timeRegex = Regex("time=(\\d+(\\.\\d+)?)")
        val match = timeRegex.find(output)
        val latency = match?.groups?.get(1)?.value

        if (latency != null) {
            val ms = latency.toDouble()
            val quality = when {
                ms < 50 -> "📶 Excellent Connection"
                ms < 100 -> "✅ Good Connection"
                ms < 200 -> "⚠️ Average Connection"
                else -> "❌ Slow Connection"
            }
            "$quality (Latency: ${"%.1f".format(ms)} ms)"
        } else {
            "⚠️ Unable to measure latency"
        }
    } catch (e: Exception) {
        handleError(e)
        "⚠️ Unable to measure latency"
    }
}

fun getCompactLatency(): String {
    return try {
        val process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8")
        val output = process.inputStream.bufferedReader().readText()
        val timeRegex = Regex("time=(\\d+(\\.\\d+)?)")
        val match = timeRegex.find(output)
        val latency = match?.groups?.get(1)?.value?.toDoubleOrNull()

        if (latency != null) {
            when {
                latency < 30 -> "Delay: ${"%.0f".format(latency)}ms"
                latency < 100 -> "Delay: ${"%.0f".format(latency)}ms"
                latency < 200 -> "Delay: ${"%.0f".format(latency)}ms"
                else -> "Latency: ${"%.0f".format(latency)}ms"
            }
        } else {
            ""
        }
    } catch (e: Exception) {
        handleError(e)
        ""
    }
}


fun getMobileSpeed(networkCapabilities: NetworkCapabilities?): String {
    return if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) {
        "${networkCapabilities.linkDownstreamBandwidthKbps / 1000} Mbps"
    } else "Not Connected to Mobile Data"
}

// 📡 Unique Insights: Internet Protocol (IP) Support
fun getIPv4v6Support(): String {
    var ipv4Supported = false
    var ipv6Supported = false

    try {
        val addresses = InetAddress.getAllByName("google.com")
        for (address in addresses) {
            if (address is Inet4Address) ipv4Supported = true
            if (address is Inet6Address) ipv6Supported = true
        }
    } catch (e: Exception) {
        handleError(e)
        return "❌ Could not determine your internet protocol support."
    }

    return when {
        ipv4Supported && ipv6Supported -> """
            ✅ Your internet supports both IPv4 & IPv6
            
            🌍 IPv4: Standard IP for most networks
            
            🌐 IPv6: Next-gen IP for faster, future-proof connectivity
        """.trimIndent()

        ipv4Supported -> """
            🌍 Only IPv4 Supported
            
            ✅ Compatible with most websites & services
            
            ⚠️ May miss out on future IPv6-only features
        """.trimIndent()

        ipv6Supported -> """
            🌐 Only IPv6 Supported
            
            ✅ Modern IP supported
            
            ⚠️ May have issues with older websites
        """.trimIndent()

        else -> "❌ No Internet Protocol Support Detected"
    }
}


fun getPacketLoss(): String {
    return try {
        val process = Runtime.getRuntime().exec("ping -c 5 google.com")
        val output = process.inputStream.bufferedReader().readText()
        val packetLoss =
            Regex("(\\d+)% packet loss").find(output)?.groupValues?.get(1)?.toIntOrNull()

        return when (packetLoss) {
            null -> "📉 Packet Loss Data Unavailable"
            0 -> "✅ No Packet Loss — Stable Network"
            in 1..20 -> "⚠️ Minor Packet Loss ($packetLoss%) — May Cause Lag"
            in 21..50 -> "❗ Moderate Packet Loss ($packetLoss%) — Noticeable Issues"
            else -> "❌ High Packet Loss ($packetLoss%) — Connection Problems Likely"
        }
    } catch (e: Exception) {
        handleError(e)
        "⚠️ Couldn't check network loss"
    }
}


fun getJitter(): String {
    return try {
        val start = System.nanoTime()
        val process = Runtime.getRuntime().exec("ping -c 5 google.com")
        process.inputStream.bufferedReader().readText()
        val end = System.nanoTime()
        val jitter = (end - start) / 1_000_000_000.0
        "%.2f ms Jitter".format(jitter)
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getCaptivePortalStatus(context: Context): String {
    return try {
        val capabilities = networkCapabilities(context)
        if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) == true) {
            "Login Required 🔒"
        } else {
            "No Captive Portal ✅"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getISPDetails(): String {
    return try {
        val url = URL("https://ipinfo.io/json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)

        val ip = json.optString("ip", "Unknown IP")
        val ispName = json.optString("org", "Unknown ISP")
        val asn = json.optString("asn", "N/A")
        val hostname = json.optString("hostname", "N/A")
        val city = json.optString("city", "Unknown City")
        val region = json.optString("region", "Unknown Region")
        val country = json.optString("country", "Unknown")
        val location = json.optString("loc", "Unknown Location")
        val postalCode = json.optString("postal", "N/A")
        val timezone = json.optString("timezone", "Unknown Timezone")

        """
        🔍 Hostname: $hostname

        🌍 Public IP: $ip
        
        🏢 ISP: $ispName
        
        🔗 ASN: $asn
        
        🏙️ City: $city
        
        📍 Region: $region
        
        🌎 Country: $country
        
        🗺️ Location (Lat, Long): $location
        
        🏢 Postal Code: $postalCode
        
        ⏰ Timezone: $timezone
        """.trimIndent()
    } catch (e: Exception) {
        handleError(e)
        "ISP Information Unavailable"
    }
}


fun checkISPStreamingServers(): String {
    val streamingCDNs = mapOf(
        "📺 YouTube" to "redirector.googlevideo.com",
        "🎬 Netflix" to "fast.com",
        "🎥 Amazon Prime" to "atv-ext-eu.amazon.com",
        "📡 Cloudflare CDN" to "one.one.one.one",
        "🔗 Akamai CDN" to "a248.e.akamai.net",
        "🎞️ Disney+" to "dssott.com",
        "🎮 PlayStation Network" to "d1ps4.network.playstation.net",
        "🎮 Xbox Live CDN" to "xbox.com",
        "🎮 ISP Game Cache" to "games.ispnet.com", // Some ISPs cache Steam/Epic Games
        "📂 ISP Software Mirror" to "mirror.isp.com", // Common for software downloads"
        "\uD83D\uDD17 Google CDN" to "dns.google"
    )

    val ispMovieServers = mapOf(
        "🎥 Australia ISP Movie" to "movies.telstra.com", // Telstra Movie Cache
        "📀 South Korea ISP Movie" to "movies.ktsat.com",
        "📀 Canada ISP Movie" to "vod.shaw.ca", // Shaw ISP Movie FTP
        "🎬 France ISP Movie" to "vod.sfr.fr", // SFR France Movie Cache
        "🎬 UAE ISP Movie" to "vod.du.ae",


        // 🔹 **South Asia**
        "🎥 India ISP Movies" to "movies.hathway.com",
        "🎬 Bangladesh ISP Movies" to "movies.link3.net",
        "🎞️ Pakistan ISP Movies" to "vod.stormfiber.com",
        "📀 Malaysia ISP Movies" to "vod.unifi.com.my",
        "🎞️ Indonesia ISP Movies" to "movies.firstmedia.com",

// 🔹 **High-Paying Countries (USA, UK, Canada, Australia, Europe)**
        "🎬 USA ISP Movie" to "movies.xfinity.com", // Comcast / Xfinity VOD
        "🎞️ UK ISP Movie" to "vod.bt.com", // BT Broadband VOD
        "📺 Germany ISP Movie" to "vod.telekom.de", // Deutsche Telekom VOD

// 🔹 **Europe & Middle East High-Demand Regions**
        "🎥 Spain ISP Movie" to "vod.movistarplus.es",
        "📀 Italy ISP Movie" to "vod.fastweb.it",
        "🎞️ Netherlands ISP Movie" to "vod.kpn.com",
        "🎥 Saudi Arabia ISP Movie" to "vod.stc.com.sa",

// 🔹 **Asia-Pacific Region**
        "🎬 Singapore ISP Movie" to "vod.singtel.com",
        "🎞️ Japan ISP Movie" to "vod.dmm.com",
        "🎬 Hong Kong ISP Movie" to "vod.nowtv.com",
        "📀 Taiwan ISP Movie" to "vod.fetnet.net",

// 🔹 **Latin America**
        "🎥 Brazil ISP Movie" to "vod.netcombo.com.br",
        "🎬 Mexico ISP Movie" to "vod.izzi.mx",
        "📀 Argentina ISP Movie" to "vod.telecentro.com.ar",

// 🔹 **Generic ISP FTP Servers**
        "📂 ISP FTP Server 1" to "ftp.myisp.net",
        "📂 ISP FTP Server 2" to "movies.myisp.net",

        // Common ISP Movie & Media FTPs
        "🎞️ ISP Movie Server 1" to "movies.ispnet.com",
        "🎞️ ISP Movie Server 2" to "media.yourisp.com",


        )

    val detectedStreamingServers = streamingCDNs.map { (service, domain) ->
        val ip = try {
            InetAddress.getByName(domain).hostAddress
        } catch (e: Exception) {
            handleError(e)
            "Not Found ❌"
        }
        "$service: $ip"
    }

    val detectedMovieServers = ispMovieServers.map { (service, domain) ->
        val ip = try {
            InetAddress.getByName(domain).hostAddress
        } catch (e: Exception) {
            handleError(e)
            "Not Found ❌"
        }
        "$service: $ip"
    }

    return """
 🌍Streaming/CDN Servers 🌍
 
 ${detectedStreamingServers.joinToString("\n")}

 📂ISP FTP Movie Servers 📂
 
${detectedMovieServers.joinToString("\n")}

    """.trimIndent()
}


fun getGatewayAddress(): String {
    return try {
        val process = Runtime.getRuntime().exec("ip route | awk '/default/ { print \$3 }'")
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val gateway = reader.readLine()?.trim()

        if (!gateway.isNullOrBlank()) {
            """
            🛣️ This is your device's default gateway — usually your router's internal IP.
            📍 Gateway IP: $gateway
            """.trimIndent()
        } else {
            "❌ No Gateway Found. You're probably not connected to a network."
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Gateway Address Unavailable"
    }
}


fun getDnsServers(context: Context): String {
    val dnsList = mutableSetOf<String>()

    try {
        // ✅ Attempt via LinkProperties (most reliable on Android 10+)
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val linkProperties = connectivityManager.getLinkProperties(network)

        linkProperties?.dnsServers?.forEach {
            dnsList.add(it.hostAddress ?: "")
        }

        // ✅ Fallback to system properties (older Android support)
        val fallbackProps = listOf("net.dns1", "net.dns2", "net.dns3", "net.dns4")
        for (prop in fallbackProps) {
            val process = Runtime.getRuntime().exec("getprop $prop")
            val dns = process.inputStream.bufferedReader().readLine()?.trim()
            if (!dns.isNullOrBlank()) {
                dnsList.add(dns)
            }
        }

    } catch (e: Exception) {
        handleError(e)
    }

    return if (dnsList.isEmpty()) "No DNS Found" else dnsList.joinToString("\n")
}


fun getMtu(): String {
    return try {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        networkInterfaces.toList().forEach { networkInterface ->
            if (networkInterface.isUp) {
                val mtu = networkInterface.mtu
                if (mtu > 0) {
                    return "$mtu bytes"
                }
            }
        }
        "Not Available"
    } catch (e: Exception) {
        handleError(e)
        "Not Available"
    }
}

fun getLocalWiFiIPAddress(context: Context): String {
    return try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No active network"

        val linkProperties =
            connectivityManager.getLinkProperties(network) ?: return "No link properties"

        val ipAddress = linkProperties.linkAddresses
            .firstOrNull { it.address is Inet4Address } // IPv4 only
            ?.address?.hostAddress

        ipAddress ?: "Not connected to Wi-Fi"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getPublicIPAddressFromIPInfo(): String {
    return try {
        val url = URL("https://ipinfo.io/json")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(response)

        json.optString("ip", "Unknown IP")
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getLocalIPAddress(context: Context): String {
    val wifiIp = getLocalWiFiIPAddress(context)
    val mobileIp = getMobileDataIPAddress()
    val allIps = getAllLocalIPAddresses()

    return """
📶 Wi-Fi IP: $wifiIp

📡 Mobile Data IP: $mobileIp

🌐 All Local IPs:
$allIps
""".trimIndent()

}

fun getMobileDataIPAddress(): String {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        interfaces.toList().forEach { networkInterface ->
            val addresses = networkInterface.inetAddresses
            addresses.toList().forEach { inetAddress ->
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.hostAddress ?: "Unknown"
                }
            }
        }
        "Not Available"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getAllLocalIPAddresses(): String {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        val ipList = mutableListOf<String>()
        interfaces.toList().forEach { networkInterface ->
            val addresses = networkInterface.inetAddresses
            addresses.toList().forEach { inetAddress ->
                if (!inetAddress.isLoopbackAddress) {
                    ipList.add(inetAddress.hostAddress ?: "Unknown")
                }
            }
        }
        ipList.joinToString("\n") // Return all IPs as a list
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getInternetUptime(): String {
    return try {
        val process = Runtime.getRuntime().exec("uptime -p")
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun checkISPTracking(): String {
    val googleUrl = "https://clients3.google.com/generate_204"
    val firefoxUrl = "https://detectportal.firefox.com/canonical.html"

    fun testUrl(url: String, expectedCode: Int): Pair<Boolean, Int?> {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = false
            connection.connect()
            val code = connection.responseCode
            Pair(code == expectedCode, code)
        } catch (e: Exception) {
            handleError(e)
            Pair(false, null)
        }
    }
    // ✅ Try Google's test first
    val (googleOk, googleCode) = testUrl(googleUrl, 204)
    return if (googleOk) {
        "✅ No ISP Tracking Detected\n"
    } else {
        // 🔁 Fallback to Firefox test
        val (firefoxOk, firefoxCode) = testUrl(firefoxUrl, 200)

        if (firefoxOk) {
            "✅ No ISP Tracking Detected (via Firefox fallback)\n"
        } else {
            "⚠️ Your ISP might be tracking or redirecting your web traffic!\n"
        }
    }
}


fun checkDPIDetection(): String {
    return try {
        val vpnTest =
            Runtime.getRuntime().exec("ping -c 1 8.8.8.8").inputStream.bufferedReader().readText()
        if (vpnTest.contains("packet loss") || vpnTest.contains("Request timeout")) {
            "⚠️ Your ISP might be blocking VPNs or inspecting traffic!"
        } else {
            "✅ No DPI Detected"
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Unable to Check DPI"
    }
}


fun checkSSLCertificateHijack(): String {
    return try {
        val url = URL("https://www.google.com")
        val connection = url.openConnection() as HttpsURLConnection
        connection.connect()

        val cert = connection.serverCertificates[0] as X509Certificate
        val issuer = cert.issuerDN.name

        if (!issuer.contains("Google") && !issuer.contains("Let's Encrypt")) {
            "⚠️ Suspicious SSL Certificate Detected! Possible MITM Attack!"
        } else {
            "✅ SSL Certificates Appear Normal"
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Unable to Check SSL Certificates"
    }
}


fun checkTransparentProxy(): String {
    return try {
        val url = URL("https://check.torproject.org")
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        val server =
            connection.getHeaderField("Via") ?: connection.getHeaderField("X-Forwarded-For")
        if (server != null) {
            "⚠️ Your internet traffic may be going through an unknown proxy!\n"
        } else {
            "✅ No Transparent Proxy Detected\n"
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Unable to Check for Proxy Interception\n"
    }
}

fun checkDNSManipulation(): String {
    return try {
        val googleDNS = InetAddress.getByName("dns.google").hostAddress
        val cloudflareDNS = InetAddress.getByName("one.one.one.one").hostAddress

        if (googleDNS != "8.8.8.8" || cloudflareDNS != "1.1.1.1") {
            "⚠️ Your ISP or Government might be hijacking DNS requests!\n"
        } else {
            "✅ DNS Requests Appear Normal\n"
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Unable to Check DNS Manipulation\n"
    }
}


fun checkInternetPrivacyAndSurveillance(): String {
    val dnsManipulation = checkDNSManipulation()
    val proxyDetection = checkTransparentProxy()
    val sslIntegrity = checkSSLCertificateHijack()
    val dpiDetection = checkDPIDetection()
    val ispTracking = checkISPTracking()

    val threatCount = listOf(
        dnsManipulation,
        proxyDetection,
        sslIntegrity,
        dpiDetection,
        ispTracking
    ).count { it.contains("⚠️") || it.contains("❌") }

    val threatLevel = when (threatCount) {
        0 -> "🟢 Low"
        1, 2 -> "🟡 Moderate"
        else -> "🔴 High"
    }

    return """
        🔐 Threat Level Summary: $threatLevel

        🔍 Fake or Redirected Websites (DNS Tampering): $dnsManipulation
        
        🔗 Invisible ISP Proxies (Traffic Interception): $proxyDetection
        
        🔒 Secure Websites Tampered (SSL Certificate Check): $sslIntegrity
        
        📡 Deep Data Scanning (Government/ISP Surveillance): $dpiDetection
        
        🕵️ Tracking & User Activity Logging: $ispTracking
    """.trimIndent()
}

@SuppressLint("HardwareIds")
fun getWiFiInformation(context: Context): String {
    return try {
        val wifiInfo = wifiInfo(context) ?: return "📶 No Wi-Fi connection available."

        val freq = wifiInfo.frequency
        val mac =
            if (wifiInfo.macAddress == "02:00:00:00:00:00") "Restricted on Android 10+" else wifiInfo.macAddress
                ?: "Unavailable"

        fun getSignalLevel(rssi: Int, numLevels: Int = 5): Int {
            val minRssi = -100
            val maxRssi = -50
            return when {
                rssi <= minRssi -> 0
                rssi >= maxRssi -> numLevels - 1
                else -> ((rssi - minRssi).toFloat() / (maxRssi - minRssi) * (numLevels - 1)).toInt()
            }
        }

        val bssid = wifiInfo.bssid ?: "Unavailable"
        val linkSpeed = "${wifiInfo.linkSpeed} Mbps"
        val supplicantState = wifiInfo.supplicantState.toString()

        val rssi = wifiInfo.rssi
        val signalLevel = getSignalLevel(rssi)
        val signalStatus = when (signalLevel) {
            4 -> "Excellent 📶"
            3 -> "Good ✅"
            2 -> "Fair ⚠️"
            1 -> "Poor ❌"
            else -> "Very Weak ❌"
        }

        val distance = if (rssi != -127 && freq > 0) {
            val exp = (27.55 - (20 * log10(freq.toDouble())) + abs(rssi)) / 20.0
            "%.1f meters".format(10.0.pow(exp))
        } else {
            "Unknown"
        }

        val encryption = when (freq) {
            in 2400..2500 -> "WPA2 / WPA3 (2.4 GHz)"
            in 5000..6000 -> "WPA3 (5 GHz)"
            else -> "Open or Unknown"
        }

        val linkSpeedTx =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "${wifiInfo.txLinkSpeedMbps} Mbps" else "N/A"
        val linkSpeedRx =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "${wifiInfo.rxLinkSpeedMbps} Mbps" else "N/A"
        val maxTxSpeed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "${wifiInfo.maxSupportedTxLinkSpeedMbps} Mbps" else "N/A"
        val maxRxSpeed =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) "${wifiInfo.maxSupportedRxLinkSpeedMbps} Mbps" else "N/A"


        val tethering = getTetheringStatus(context)
        """
        🔐 Security: $encryption (using ${freq}MHz band)
        
        📶 Signal: $signalStatus ($rssi dBm, Level $signalLevel/4)
        
        📏 Estimated distance from router: $distance
        
        🔗 Device MAC: $mac
        
        📡 Router MAC (BSSID): $bssid
                
        🔄 Connection State: $supplicantState
        
        🚀 Speed: $linkSpeed
        
        📤 Upload: $linkSpeedTx
       
        📥 Download: $linkSpeedRx
        
        🔼 Max Upload: $maxTxSpeed
        
        🔽 Max Download: $maxRxSpeed
        
        🔥 Hotspot Status: $tethering
        
        """.trimIndent()
    } catch (e: Exception) {
        handleError(e)
        "⚠️ Unable to retrieve Wi-Fi info"
    }
}

fun getTetheringStatus(context: Context): String {
    return try {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method = wifiManager.javaClass.getDeclaredMethod("isWifiApEnabled")
        method.isAccessible = true
        val isHotspotEnabled = method.invoke(wifiManager) as Boolean

        if (isHotspotEnabled) "Hotspot (Tethering) Active 🔥"
        else "Hotspot Off ❌"
    } catch (e: Exception) {
        handleError(e)
        "Tethering Status Unknown ⚠️"
    }
}


private fun wifiInfo(context: Context): WifiInfo? {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        capabilities?.transportInfo as? WifiInfo
    } else {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.connectionInfo
    }
    return wifiInfo
}

fun pingPopularServers(): String {
    val servers = listOf(
        "Google" to "8.8.8.8",
        "Cloudflare" to "1.1.1.1",
        "OpenDNS" to "208.67.222.222",
        "Quad9" to "9.9.9.9",
        "Level3" to "4.2.2.1",
        "NextDNS" to "45.90.28.0",
        "AdGuard" to "94.140.14.14",
        "Yandex" to "77.88.8.8",
        "Neustar" to "156.154.70.1",
        "CleanBrowsing" to "185.228.168.9",
        "Comodo Secure" to "8.26.56.26"
    )

    val results = mutableListOf<String>()

    for ((name, ip) in servers) {
        val result = try {
            val process = ProcessBuilder("ping", "-c", "3", "-W", "3", ip) // -W 3 = timeout 3s
                .redirectErrorStream(true)
                .start()

            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()

            val success = process.waitFor() == 0
            val avg = Regex("= .*?/([\\d.]+)/").find(output)?.groupValues?.get(1)

            if (success && avg != null)
                "✅ $ip ($name): $avg ms"
            else
                "❌ $ip ($name): Timeout"

        } catch (e: Exception) {
            "❌ $ip ($name): Error"
        }

        results.add(result)
    }

    return results.joinToString("\n")
}

data class NetworkUsage(
    val period: String,
    val wifiUsage: String,
    val mobileUsage: String
)

fun getReadableUsage(context: Context, bytes: Long): String {
    return Formatter.formatShortFileSize(context, bytes)
}

private suspend fun queryUsageSmart(
    context: Context,
    startTime: Long,
    endTime: Long,
    networkType: Int
): Long = withContext(Dispatchers.IO) {
    try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }

        if (mode != AppOpsManager.MODE_ALLOWED) {
            return@withContext 0L
        }

        val statsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val networkStats = statsManager.querySummary(networkType, null, startTime, endTime)

        var totalBytes = 0L
        val bucket = NetworkStats.Bucket()
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket)
            totalBytes += bucket.rxBytes + bucket.txBytes
        }

        networkStats.close()

        // ✅ Fallback to TrafficStats if result is unexpectedly 0
        if (totalBytes == 0L && networkType == ConnectivityManager.TYPE_MOBILE) {
            TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        } else {
            totalBytes
        }

    } catch (e: Exception) {
        handleError(e)
        if (networkType == ConnectivityManager.TYPE_MOBILE) {
            TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        } else {
            TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() -
                    (TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes())
        }
    }
}


suspend fun getNetworkUsageStatsThrottled(
    context: Context,
    onUpdate: (NetworkUsage) -> Unit
) {
    if (!hasUsageStatsPermission(context)) {
        onUpdate(
            NetworkUsage(
                period = "Please allow Network Usage Stats from Settings → App Drawer → Usage Access.",
                wifiUsage = "",
                mobileUsage = ""
            )
        )
        return
    }

    val calendar = Calendar.getInstance()

    fun range(start: Calendar.() -> Unit, end: Calendar.() -> Unit): Pair<Long, Long> {
        val startCal = Calendar.getInstance().apply(start)
        val endCal = Calendar.getInstance().apply(end)
        return startCal.timeInMillis to endCal.timeInMillis
    }

    val periods = listOf(
        "Today" to range({
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }, { }),
        "Yesterday" to range({
            add(Calendar.DATE, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }, {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }),
        "This Week" to range({
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }, { }),
        "Last Week" to range({
            add(Calendar.WEEK_OF_YEAR, -1)
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }, {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }),
        "${calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())} (This Month)" to range({
            set(Calendar.DAY_OF_MONTH, 1)
        }, { }),
        run {
            val prev = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
            val monthName = prev.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            "$monthName (Last Month)" to range({
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 1)
            }, {
                set(Calendar.DAY_OF_MONTH, 1)
            })
        },
        "${calendar.get(Calendar.YEAR)} (This Year)" to range({
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }, { }),
        "${calendar.get(Calendar.YEAR) - 1} (Last Year)" to range({
            set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }, {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 1)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
        }),
        "Lifetime" to range({
            set(Calendar.YEAR, 2010)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }, { })
    )

    for ((label, range) in periods) {
        val (start, end) = range
        delay(200)

        val wifi = queryUsageSmart(context, start, end, ConnectivityManager.TYPE_WIFI)
        val mobile = queryUsageSmart(context, start, end, ConnectivityManager.TYPE_MOBILE)

        onUpdate(
            NetworkUsage(
                period = label,
                wifiUsage = getReadableUsage(context, wifi),
                mobileUsage = getReadableUsage(context, mobile)
            )
        )
    }
}

// Add this function to check if usage stats permission is granted
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}


fun calculateInternetHealthScore(
    latencyMs: Double,
    jitterMs: Double,
    packetLossPercent: Int,
    downloadMbps: Double,
    uploadMbps: Double
): String {
    var score = 100

    if (latencyMs > 100) score -= 15
    if (jitterMs > 30) score -= 15
    if (packetLossPercent > 5) score -= 30
    if (downloadMbps < 10) score -= 20
    if (uploadMbps < 2) score -= 20

    score = score.coerceIn(0, 100)

    val rating = when {
        score >= 85 -> "🌟 Excellent"
        score >= 65 -> "✅ Good"
        score >= 40 -> "⚠️ Average"
        else -> "❌ Poor"
    }

    return "📊 Internet Health Score: $score/100\n🏅 Rating: $rating"
}
