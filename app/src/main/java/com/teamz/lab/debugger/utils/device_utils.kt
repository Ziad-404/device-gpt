package com.teamz.lab.debugger.utils

import android.Manifest
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaCodecList
import android.opengl.GLES10
import android.opengl.GLSurfaceView
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.Choreographer
import com.teamz.lab.debugger.R
import com.teamz.lab.debugger.utils.string
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import javax.microedition.khronos.opengles.GL10
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun openSettings(context: Context, action: String) {
    try {
        val intent = Intent(action)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context, "This setting is not available on your device.", Toast.LENGTH_SHORT
            ).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, context.string(R.string.toast_error_opening_setting, e.message ?: ""), Toast.LENGTH_SHORT).show()
        handleError(e)
    }
}

fun isDeviceRooted(): String {
    val paths = arrayOf(
        "/system/app/Superuser.apk",
        "/system/xbin/su",
        "/system/bin/su",
        "/system/sbin/su",
        "/sbin/su",
        "/vendor/bin/su"
    )
    return if (paths.any { File(it).exists() }) "Yes" else "No"
}


fun getRamUsage(context: Context): String {
    val memoryInfo = android.app.ActivityManager.MemoryInfo()
    val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    activityManager.getMemoryInfo(memoryInfo)

    val usedRam = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024)
    val totalRam = memoryInfo.totalMem / (1024 * 1024)
    val usagePercent = (usedRam.toFloat() / totalRam * 100).toInt()

    return "$usedRam MB / $totalRam MB ($usagePercent%)"
}


fun getMemoryAndStorageInfo(context: Context): String {
    val ramUsage = getRamUsage(context)
    val availableStorage = getAvailableStorage()
    val storageSpeed = testStorageSpeed(context)

    return """
        💾 RAM Usage: $ramUsage
        
        💿 Available Storage: $availableStorage
        
        🔄 Storage Speed: $storageSpeed
    """.trimIndent()
}

fun getDateTimeInfo(context: Context): String {
    val currentTime = System.currentTimeMillis()
    val timeZone = java.util.TimeZone.getDefault().displayName
    val formatter = SimpleDateFormat("MMMM dd, yyyy - h:mm a", Locale.getDefault())

    val formattedTime = formatter.format(Date(currentTime))

    val uptimeMillis = SystemClock.elapsedRealtime()
    val uptimeHours = uptimeMillis / (1000 * 60 * 60)
    val uptimeMinutes = (uptimeMillis / (1000 * 60)) % 60
    val formattedUptime = "$uptimeHours hours, $uptimeMinutes minutes"

    val rtcTimeMillis = SystemClock.currentThreadTimeMillis()
    val formattedRtcTime = formatter.format(Date(rtcTimeMillis))

    val lastBootMillis = currentTime - uptimeMillis
    val lastBootTime = formatter.format(Date(lastBootMillis))

    return """
    🕒 Current Time (Your Phone Shows): $formattedTime
    
    🌍 Time Zone (Region): $timeZone
    
    ⏳ System Uptime (Time Since Device Turned On): $formattedUptime
    
    🕰 RTC Time (Internal Clock): $formattedRtcTime
    
    🔄 Last Boot Time (When Device Was Last Restarted): $lastBootTime
    
    ⏰ Automatic Time Sync: ${isTimeAutoSyncEnabled(context)} 
""".trimIndent()

}


val buildDate: String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(Build.TIME))

val socManufacturer = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MANUFACTURER else "Unavailable"
val socModel = if (Build.VERSION.SDK_INT >= 31) Build.SOC_MODEL else "Unavailable"
val sku = if (Build.VERSION.SDK_INT >= 31) Build.SKU else "Unavailable"
val odmSku = if (Build.VERSION.SDK_INT >= 31) Build.ODM_SKU else "Unavailable"

fun getDeviceInfoString(context: Context): String {
    return """
📱 Device Name (Model): ${Build.MODEL}

🏭 Manufacturer (Company): ${Build.MANUFACTURER}

🔰 Brand (Marketing Name): ${Build.BRAND}

🎯 Product Series (Device Series): ${Build.PRODUCT}

🔍 Internal Name (Device Code): ${Build.DEVICE}

🖥️ Hardware Type (CPU Board): ${Build.HARDWARE}

🔩 Motherboard (Mainboard ID): ${Build.BOARD}

🛠️ Bootloader Version (Startup Firmware): ${Build.BOOTLOADER}

📦 Build ID (Software Version): ${Build.ID}

🔖 System Type (Build Type): ${Build.TYPE}

⚙️ Processor Vendor: $socManufacturer

🧠 CPU Model: $socModel

🔋 Battery Manager (Power SKU): $sku

🔧 Device Configuration (ODM SKU): $odmSku

🗓️ Manufacturing Date (Build Time): $buildDate

📡 Radio Version (Modem Firmware): ${Build.getRadioVersion()}

📟 CPU Architectures Supported: ${Build.SUPPORTED_ABIS.joinToString(", ")}

📟 32-bit CPU Support: ${Build.SUPPORTED_32_BIT_ABIS.joinToString(", ")}

📟 64-bit CPU Support: ${Build.SUPPORTED_64_BIT_ABIS.joinToString(", ")}

📌 Android Version: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})

🔠 Codename (Dev Stage): ${Build.VERSION.CODENAME}

📶 Base OS (Underlying Android): ${Build.VERSION.BASE_OS ?: "N/A"}

📆 Build Number (Incremental Version): ${Build.VERSION.INCREMENTAL ?: "N/A"}

🧪 Developer Preview SDK Level: ${Build.VERSION.PREVIEW_SDK_INT}

🔐 Security Patch Level: ${Build.VERSION.SECURITY_PATCH}

✅ Google Play Certified: ${isPlayStoreCertified(context)}

🚀 System Fingerprint (Unique Build ID): ${Build.FINGERPRINT}
""".trimIndent()
}


fun copyToClipboard(context: Context, body: String, title: String) {
    val clipboard =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText(title, body)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, context.string(R.string.toast_copied_to_clipboard), Toast.LENGTH_SHORT).show()
}

// getGpuInfo.kt
fun createGpuInfoSurfaceView(context: Context, onGpuInfoReady: (String) -> Unit): GLSurfaceView {
    val surfaceView = GLSurfaceView(context)

    surfaceView.setEGLContextClientVersion(1)
    surfaceView.setRenderer(object : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(
            gl: GL10?,
            config: javax.microedition.khronos.egl.EGLConfig?
        ) {
            val renderer = GLES10.glGetString(GLES10.GL_RENDERER)
            val vendor = GLES10.glGetString(GLES10.GL_VENDOR)
            val version = GLES10.glGetString(GLES10.GL_VERSION)
            val extensions = GLES10.glGetString(GLES10.GL_EXTENSIONS)

            val gpuInfo = """
                🎮 GPU Renderer: $renderer

                🏭 GPU Vendor: $vendor

                🔢 OpenGL Version: $version

                🧩 Extensions: ${extensions?.take(100)}...
            """.trimIndent()

            onGpuInfoReady(gpuInfo)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
        override fun onDrawFrame(gl: GL10?) {}
    })

    surfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    return surfaceView
}

fun getCpuInfo(): String {
    val coreCount = Runtime.getRuntime().availableProcessors()
    val cpuArch = getCpuArchitecture()
    val maxClockSpeedMHz = getMaxCpuClockSpeedMHz()
    val maxClockSpeedGHz = if (maxClockSpeedMHz > 0) {
        String.format(Locale.getDefault(), "%.2f GHz", maxClockSpeedMHz / 1000.0)
    } else {
        "Unavailable"
    }
    val minClockSpeed = getMinCpuClockSpeed()
    val governor = getCpuGovernor()
    val cpuModel = getCpuModel()
    val cpuVendor = getCpuVendor()
    val bogoMips = getBogoMips()
    val cpuFeatures = getCpuFeatures()

    // Real-time frequency per core
    val frequencies = getCpuFrequencies(coreCount)
    val maxFrequencies = getMaxFrequenciesPerCore(coreCount)

    val frequencyInfo = frequencies.mapIndexed { index, currentMHz ->
        val maxMHz = maxFrequencies.getOrNull(index) ?: -1

        if (currentMHz != -1 && maxMHz > 0) {
            val percent = (currentMHz * 100) / maxMHz
            "🧠 Core $index: $currentMHz / $maxMHz MHz ($percent%)"
        } else if (currentMHz != -1) {
            "🧠 Core $index: $currentMHz MHz"
        } else {
            "🧠 Core $index: N/A"
        }
    }.joinToString("\n")


    // Total current performance info
    val activeCores = frequencies.filter { it != -1 }
    val totalUsedMHz = activeCores.sum()
    val totalCapacityMHz = getMaxFrequenciesPerCore(coreCount).sum()
    val usedGHz = totalUsedMHz.toDouble() / 1000
    val capacityGHz = totalCapacityMHz.toDouble() / 1000
    val usagePercent = if (totalCapacityMHz > 0) (usedGHz / capacityGHz * 100).toInt() else 0

    val summaryInfo = if (capacityGHz > 0) {
        "📈 CPU Usage Summary:\nUsed: %.2f GHz / %.2f GHz (%d%%)".format(
            usedGHz, capacityGHz, usagePercent
        )
    } else {
        "📈 CPU Usage Summary: Unavailable"
    }
    return """
🧩 Here's a technical peek inside your phone's brain (CPU)!

🖥️ Processor Cores: $coreCount
More cores help your device handle more tasks at once.

🔧 System Type: $cpuArch
This is the CPU architecture — tells how your processor is designed.

⚡ Max Speed: $maxClockSpeedGHz GHz (Best Performance)
The highest speed your CPU can run when under heavy load.

💤 Min Speed: $minClockSpeed GHz (Power Saving Mode)
The lowest speed when your phone is idle to save battery.

⚙️ Performance Mode: $governor
Controls how your CPU balances performance and battery.

🏭 Processor Maker: $cpuVendor
The brand of the CPU inside your device.

🏷️ Model Name: $cpuModel
The specific processor model powering your phone.

🔥 Estimated Power: $bogoMips BogoMIPS
Rough indicator of CPU computing power (used for benchmarking).

🧩 Supported Features: $cpuFeatures
Hardware capabilities like NEON, AES, etc.

📊 Real-time CPU Frequencies:
Shows how fast each core is currently running.
$frequencyInfo

$summaryInfo

(ℹ️ Note: If a core shows "N/A", it may be temporarily turned off to save power.)
""".trimIndent()
}

fun getMaxFrequenciesPerCore(coreCount: Int): List<Int> {
    val maxFrequencies = mutableListOf<Int>()
    for (i in 0 until coreCount) {
        val path = "/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq"
        try {
            val freq = File(path).readText().trim().toInt() / 1000 // Convert to MHz
            maxFrequencies.add(freq)
        } catch (_: Exception) {
            maxFrequencies.add(-1)
        }
    }
    return maxFrequencies
}

fun getCompactCpuInfo(): String {
    val coreCount = Runtime.getRuntime().availableProcessors()
    val freqs = getCpuFrequencies(coreCount)

    val activeCores = freqs.filter { it != -1 }
    val idleCores = freqs.count { it == -1 }
    val totalUsedMHz = activeCores.sum()
    val totalCapacityMHz = getMaxFrequenciesPerCore(coreCount).sum()

    if (totalCapacityMHz <= 0 || activeCores.isEmpty()) return ""

    val usedGHz = totalUsedMHz.toDouble() / 1000
    val capacityGHz = totalCapacityMHz.toDouble() / 1000
    val percentUsed = (usedGHz / capacityGHz * 100).toInt()

    return "CPU: %.2f / %.2f GHz (%d%%)• %d active cores%s".format(
        usedGHz,
        capacityGHz,
        percentUsed,
        activeCores.size,
        if (idleCores > 0) " • $idleCores idle cores" else ""
    )
}


private fun getCpuFrequencies(coreCount: Int): MutableList<Int> {
    val frequencies = mutableListOf<Int>()
    for (i in 0 until coreCount) {
        val path = "/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"
        try {
            val freq = File(path).readText().trim().toInt() / 1000
            frequencies.add(freq)
        } catch (e: Exception) {
            handleError(e)
            frequencies.add(-1)
        }
    }
    return frequencies
}


/**
 * Gets CPU Architecture
 */
fun getCpuArchitecture(): String {
    return Build.SUPPORTED_ABIS.joinToString(", ")
}

/**
 * Gets the Maximum Clock Speed (in GHz) of the CPU
 */
fun getMaxCpuClockSpeedMHz(): Int {
    return try {
        val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
        if (file.exists()) {
            file.readText().trim().toInt() / 1000
        } else {
            -1
        }
    } catch (e: Exception) {
        handleError(e)
        -1
    }
}


/**
 * Extract CPU Model
 */
fun getCpuModel(): String {
    return try {
        val cpuInfo = File("/proc/cpuinfo").readLines()
        val modelLine =
            cpuInfo.firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
        modelLine?.split(":")?.get(1)?.trim() ?: "Unknown"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


/**
 * Gets the Minimum Clock Speed (in GHz) of the CPU
 */
fun getMinCpuClockSpeed(): String {
    return try {
        val file = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")
        if (file.exists()) {
            val freq = file.readText().trim().toLong() / 1_000_000.0 // ✅ Convert to Double
            String.format(Locale.getDefault(), "%.2f GHz", freq) // ✅ Format properly as Double
        } else {
            "Unavailable"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

/**
 * Extract CPU Vendor
 */
fun getCpuVendor(): String {
    return try {
        val cpuInfo = File("/proc/cpuinfo").readLines()
        val vendorLine =
            cpuInfo.firstOrNull { it.startsWith("vendor_id") || it.startsWith("Processor") }
        vendorLine?.split(":")?.get(1)?.trim() ?: "Unknown"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

/**
 * Extract BogoMIPS (Bogus Million Instructions Per Second)
 */
fun getBogoMips(): String {
    return try {
        val cpuInfo = File("/proc/cpuinfo").readLines()
        val bogoMipsLine = cpuInfo.firstOrNull { it.startsWith("BogoMIPS") }
        bogoMipsLine?.split(":")?.get(1)?.trim() ?: "Unknown"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


/**
 * Gets the CPU Governor (Performance mode of CPU)
 */
fun getCpuGovernor(): String {
    return try {
        val file = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
        if (file.exists()) file.readText().trim() else "Unavailable"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


/**
 * Extract CPU Features (e.g., NEON, SSE, etc.)
 */
fun getCpuFeatures(): String {
    return try {
        val cpuInfo = File("/proc/cpuinfo").readLines()
        val featuresLine =
            cpuInfo.firstOrNull { it.startsWith("Features") || it.startsWith("flags") }
        featuresLine?.split(":")?.get(1)?.trim()?.replace(" ", ", ") ?: "Unavailable"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun isUsbDebuggingEnabled(context: Context): String {
    return try {
        val adbEnabled =
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0)
        if (adbEnabled == 1) "Enabled" else "Disabled"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getThermalStatus(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val status = when (powerManager.currentThermalStatus) {
            PowerManager.THERMAL_STATUS_LIGHT -> "🔆 Light Throttling (Performance Reduced Slightly)"
            PowerManager.THERMAL_STATUS_MODERATE -> "🌡 Moderate Throttling (Performance Impacted)"
            PowerManager.THERMAL_STATUS_SEVERE -> "🔥 Severe Throttling (Performance Significantly Affected)"
            PowerManager.THERMAL_STATUS_CRITICAL -> "🚨 Critical Throttling (Risk of System Instability)"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "⚠️ Emergency Shutdown Risk (Device Overheating!)"
            else -> "✅ Normal Temperature (No Throttling)"
        }

        val cpuTemp = getCpuTemperature()

        """
        🌡 Thermal Status: $status
        
        🖥 CPU Temperature: $cpuTemp°C
        """.trimIndent()
    } else {
        "❌ Thermal Monitoring Not Supported (Requires Android 10+)"
    }
}

/**
 * Gets the CPU temperature by reading system files (Only works on rooted or some custom devices).
 */
fun getCpuTemperature(): String {
    return try {
        val file = File("/sys/class/thermal/thermal_zone0/temp")
        if (file.exists()) {
            val temp = file.readText().trim().toFloat() / 1000 // Convert from millidegree to degree
            "%.1f".format(temp)
        } else {
            "Unavailable"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun getCompactPowerState(context: Context): String {
    return try {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val saver = if (powerManager.isPowerSaveMode) "⚡Saver On" else "⚡Saver Off"
        val doze = if (powerManager.isDeviceIdleMode) "😴Dozing" else "⏱️Doze inactive"
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_LIGHT -> "Light"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
                PowerManager.THERMAL_STATUS_SEVERE -> "Hot"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Overheat"
                else -> "Normal"
            }
        } else {
            "N/A"
        }

        "$thermal • $saver • $doze"
    } catch (e: Exception) {
        handleError(e)
        "N/A"
    }
}


fun getCompactBatteryStatus(context: Context): String {
    return try {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryIntent =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val currentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val chargeCounter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val tempC = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1) / 10.0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

        val watts = if (voltage > 0 && currentNow != 0) {
            (voltage / 1000.0) * (abs(currentNow) / 1_000_000.0)
        } else -1.0

        val powerStr = if (watts > 0) "${"%.1f".format(watts)}W" else "N/A"

        val batteryStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging at $powerStr"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging at $powerStr"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown Status"
        }
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Idle"
        }

        val estimatedTime = if (chargeCounter > 0 && capacity in 1..99 && chargingType != "Idle") {
            val chargeRemaining = 100 - capacity
            val timeRemaining = (chargeRemaining * 0.5).toInt()
            "$timeRemaining min left"
        } else ""

        // 📦 Final compact battery summary
        val message = if (estimatedTime.isNotEmpty()) {
            "$batteryStatus • ⏳ $estimatedTime • 🔥 ${"%.1f".format(tempC)}°C"
        } else {
            "$batteryStatus • 🔥 ${"%.1f".format(tempC)}°C"
        }
        return message;

    } catch (e: Exception) {
        handleError(e)
        "Battery Info N/A"
    }
}

fun getBatteryChargingInfo(context: Context): String {
    return try {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentNow =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) // µA
        val chargeCounter =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) // µAh
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) // %

        val batteryIntent =
            context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1 // mV
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val temperature =
            (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1) / 10.0 // °C
        val health = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val tech = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val power = if (voltage > 0 && currentNow != 0) {
            val watts = (voltage / 1000.0) * (abs(currentNow) / 1_000_000.0) // Watts (W)
            "%.2f W".format(watts)
        } else "Not Available"

        // 🏆 Battery Health Status
        val batteryHealth = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good ✅"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat 🔥"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead ☠️"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Overvoltage ⚡"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure ❌"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold ❄️"
            else -> "Unknown"
        }

        // 🔌 Charging Type (USB, AC, Wireless)
        val chargingType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB ⚡"
            BatteryManager.BATTERY_PLUGGED_AC -> "Fast Charging 🔋"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless ⚡"
            else -> "Not Charging"
        }

        // 🔋 Battery Status
        val batteryStatus = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging at $power"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging at $power"
            BatteryManager.BATTERY_STATUS_FULL -> "Battery Full 🎉"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown Status"
        }

        // ⏳ Estimated Charge Time
        val estimatedTime =
            if (status == BatteryManager.BATTERY_STATUS_CHARGING && chargeCounter > 0) {
                val chargeRemaining = 100 - capacity // Percentage left to charge
                val timeRemaining = (chargeRemaining * 0.5).toInt() // Assuming ~30min per 10%
                "$timeRemaining min left"
            } else "N/A"

        // ⚠️ This is an ESTIMATE based on capacity, not real prediction
        // Battery life depends on many factors: usage patterns, charging habits, temperature, etc.
        val predictedLife = if (capacity >= 80) {
            "~1+ year (Estimated based on capacity)"
        } else {
            "~6–12 months (Estimated based on capacity)"
        }

        """
        🔋 Battery Status: $batteryStatus
        🔌 Charge Type: $chargingType
        🔥 Battery Temp: $temperature°C
        📊 Battery Health: $batteryHealth
        🔬 Battery Tech: $tech
        ⏳ Estimated Full Charge: $estimatedTime
        ${getBatteryCycleEstimate(context)}
        📅 Predicted Battery Life Remaining: $predictedLife
        """.trimIndent()

    } catch (e: Exception) {
        handleError(e)
        "Battery Info Unavailable"
    }
}


fun testStorageSpeed(context: Context): String {
    return try {
        val start = System.currentTimeMillis()
        val file = File(context.filesDir, "speedtest.tmp")
        file.writeText("Test")
        file.readText()
        file.delete()
        val end = System.currentTimeMillis()
        "RW Speed: ${end - start}ms"
    } catch (e: Exception) {
        handleError(e)
        "Failed"
    }
}


fun getFPS(callback: (Int) -> Unit) {
    var frameCount = 0
    var lastTimestamp = System.nanoTime()

    val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            frameCount++

            val currentTime = System.nanoTime()
            val elapsedSeconds = (currentTime - lastTimestamp) / 1_000_000_000.0

            if (elapsedSeconds >= 1.0) {
                val fps = frameCount / elapsedSeconds
                callback(fps.roundToInt())

                // Reset for next second
                frameCount = 0
                lastTimestamp = currentTime
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    Choreographer.getInstance().postFrameCallback(frameCallback)
}


fun getFrameDropRate(callback: (String) -> Unit) {
    var totalFrames = 0
    var droppedFrames = 0
    var lastTimestamp = System.nanoTime()

    val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            totalFrames++

            // Check if the frame is delayed (16ms per frame in 60Hz)
            val elapsedMs = (frameTimeNanos - lastTimestamp) / 1_000_000
            if (elapsedMs > 16) {
                droppedFrames++
            }
            lastTimestamp = frameTimeNanos

            val dropRate = if (totalFrames > 0) {
                (droppedFrames.toDouble() / totalFrames) * 100
            } else 0.0

            callback(
                "⚡ Smoothness: $droppedFrames drops (${
                    String.format(
                        Locale.getDefault(), "%.1f", dropRate
                    )
                }% stutter)"
            )

            // Continue monitoring
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    Choreographer.getInstance().postFrameCallback(frameCallback)

}

fun getCompactFpsAndDropRate(callback: (String) -> Unit) {
    var totalFrames = 0
    var droppedFrames = 0
    var lastTimestamp = System.nanoTime()
    var frameCount = 0
    var startTime = System.nanoTime()
    var fps = 60
    var dropRate: Double

    val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            totalFrames++

            // Calculate the elapsed time between frames (in milliseconds)
            val elapsedMs = (frameTimeNanos - lastTimestamp) / 1_000_000
            if (elapsedMs > 16) {  // If the frame took more than 16ms, it's considered a dropped frame
                droppedFrames++
            }
            lastTimestamp = frameTimeNanos
            frameCount++

            // Calculate FPS every second
            val currentTime = System.nanoTime()
            val elapsedSeconds = (currentTime - startTime) / 1_000_000_000.0

            if (elapsedSeconds >= 1.0) {
                fps = (frameCount / elapsedSeconds).toInt()
                // Reset frame count and update start time after 1 second
                frameCount = 0
                startTime = currentTime
            }

            // Calculate drop rate as percentage of dropped frames
            dropRate = if (totalFrames > 0) {
                (droppedFrames.toDouble() / totalFrames) * 100.00
            } else 0.0

            // Prepare the formatted output for FPS and Drop Rate
            callback(
                "FPS: $fps • Drop Rate: ${String.format(Locale.getDefault(), "%.1f", dropRate)}%"
            )

            // Continue monitoring frames
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    // Post the frame callback to begin monitoring
    Choreographer.getInstance().postFrameCallback(frameCallback)
}

fun hasGNSS(context: Context): Boolean {
    return try {
        val field = PackageManager::class.java.getField("FEATURE_GNSS")
        val featureGNSS = field.get(null) as String
        context.packageManager.hasSystemFeature(featureGNSS)
    } catch (e: Exception) {
        handleError(e)
        false // Feature not available (Older Android versions)
    }
}


fun getSecurityInfo(context: Context): String {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    // 🛡️ SELinux Status
    val isSELinuxEnforced = try {
        val process = Runtime.getRuntime().exec("getenforce")
        val result = process.inputStream.bufferedReader().readText().trim()
        result.equals("Enforcing", ignoreCase = true)
    } catch (e: Exception) {
        handleError(e)
        false
    }

    // 🆔 Device Admin Check
    val hasDeviceAdmin = devicePolicyManager.activeAdmins?.isNotEmpty() ?: false

    // 🔐 Encryption Status
    val encryptionStatus = when (devicePolicyManager.storageEncryptionStatus) {
        DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> "✅ Your device storage is fully protected"
        DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> "✅ Your personal data is securely protected (per user/profile)"
        DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> "❌ Storage is not protected. Your files may be at risk"
        DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> "⚠️ This device doesn't support storage protection"
        else -> "❓ Unable to check storage protection status"
    }

    // 👣 Motion While Locked
    val motionLog = detectMotionWhileLocked(context)

    // 🧠 App Permission Heatmap
    val permissionRadar = getPermissionHeatmap(context)

    // 📎 Clipboard Spy Warning
    val clipboardStatus = detectClipboardAccess()

    // 🧱 System File Tampering Check
    val tamperCheck = checkSystemTampering()

    // 🛡️ Offline Malware Signature Scan
    val malwareScan = detectOfflineMalware(context)

    return """
🛡️ System Protection (SELinux):
${if (isSELinuxEnforced) "✅ Your system protection is active and keeping things safe" else "❌ Security shield is off — less protection against threats"}

👮 Admin Access (Phone Owner):
${if (hasDeviceAdmin) "✅ You're the verified owner of this device" else "❌ No admin set — features may be limited"}

🔐 Data Safety (Phone Storage):
$encryptionStatus

🧠 Which Apps Can Peek? (Permission Radar):
$permissionRadar

📎 Clipboard Safety & Spy Detection (Copy-Paste Checker):
$clipboardStatus

🧱 System Health Check:
$tamperCheck

🛡️ Malware Scan (Offline Check):
$malwareScan

👣 Motion While Locked:
$motionLog
""".trimIndent()

}


suspend fun getLocationAndGPSInfoAsync(context: Context): String {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val packageManager = context.packageManager

    val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    val hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    val hasNetworkLocation =
        packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK)
    val hasPassiveLocation = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION)

    val hasGNSS = hasGNSS(context)

    // Async-friendly last location check
    val lastKnownLocation = lastKnownLocation(context)

    return """
        ✅ GPS (Satellite) Enabled: ${if (isGPSEnabled) "Yes" else "No"}
        
        🌐 Network-based Location: ${if (isNetworkEnabled) "Yes" else "No"}
        
        🛰️ GNSS (Advanced Satellite Support): ${if (hasGNSS) "Yes" else "No"}
        
        🧭 Built-in GPS Hardware: ${if (hasGPS) "Yes" else "No"}
        
        📡 Wi-Fi / Cell Tower Location: ${if (hasNetworkLocation) "Yes" else "No"}
        
        🌍 Battery Saver Mode Support: ${if (hasPassiveLocation) "Yes" else "No"}
        
        📌 Last Known Location: $lastKnownLocation
    """.trimIndent()
}

suspend fun lastKnownLocation(context: Context): String {
    val lastKnownLocation = if (context.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedClient.lastLocation.await()
            if (location != null) {
                "latitude : ${location.latitude}, longitude: ${location.longitude}"
            } else {
                "Not Available"
            }
        } catch (e: Exception) {
            handleError(e)
            "Error fetching location"
        }
    } else {
        "Permission Not Granted"
    }
    return lastKnownLocation
}

fun checkSystemTampering(): String {
    val suspiciousPaths = listOf(
        "/system/bin/.ext", "/system/etc/init.d/99SuperSUDaemon",
        "/system/xbin/daemonsu", "/etc/hosts"
    )
    val tampered = suspiciousPaths.filter { File(it).exists() }

    return if (tampered.isEmpty()) {
        "✅ System files look clean"
    } else {
        "⚠️ Modified system files found:\n${tampered.joinToString("\n")}"
    }
}

fun detectClipboardAccess(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        "✅ Clipboard auto-clears after a while (Android 11+). Safe."
    } else {
        "⚠️ Clipboard might be accessed by background apps (Pre Android 11)."
    }
}

fun detectOfflineMalware(context: Context): String {
    val knownMalwarePackages = listOf(
        "com.spy.fakeapp", "com.sneaky.keylogger", "com.hidden.sniffer"
    )
    val installed = getInstalledApps(context)
    val matches = knownMalwarePackages.filter { installed.contains(it) }

    return if (matches.isEmpty()) {
        "✅ No known malicious apps found (offline scan)"
    } else {
        "🚨 Malware Signatures Detected:\n${matches.joinToString("\n")}"
    }
}


fun getPermissionHeatmap(context: Context): String {
    val pm = context.packageManager
    val dangerousPermissions = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_SMS
    )

    val flaggedApps = mutableMapOf<String, MutableList<String>>()
    val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    for (app in installedApps) {
        val appName = app.loadLabel(pm).toString()
        val granted = mutableListOf<String>()

        for (perm in dangerousPermissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                granted.add(perm.substringAfterLast('.'))
            }
        }

        if (granted.isNotEmpty()) {
            flaggedApps[appName] = granted
        }
    }

    return if (flaggedApps.isEmpty()) {
        "✅ No apps with sensitive permissions detected."
    } else {
        flaggedApps.entries.joinToString("\n\n") { (app, perms) ->
            "📱 $app\n🔐 Permissions: ${perms.joinToString(", ")}"
        }
    }
}


fun getCameraMicSpeakerFlashInfo(context: Context): String {
    val packageManager = context.packageManager
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // 🎤 Check Microphone Features
    val hasMic = packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    val supportsNoiseCancellation =
        packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
    val supportsEchoCancellation =
        packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)

    // 📷 Check Camera Features
    val hasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    val hasBackCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    val supportsAutofocus = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)
    val supportsFaceDetection =
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_CAPABILITY_MANUAL_SENSOR)

    // 🔦 Flashlight Capabilities
    val hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    val supportsTorchMode =
        packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_LEVEL_FULL)

    // 🔊 Speaker & Audio Output
    val hasSpeaker = audioManager.mode != AudioManager.MODE_IN_CALL
    val supportsStereoSpeakers =
        packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)
    val hasExternalAudioOutput = packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

    // 🎙️ Check Active Usage (Microphone & Camera)
    val micCamStatus = isCameraOrMicActive(context)

    // 📷 Check for Optical Zoom & Ultra-Wide Camera using Camera2 API
    var supportsOpticalZoom = "❌ No Optical Zoom"
    var supportsUltraWide = "❌ No Ultra-Wide Camera"

    try {
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)

            // 🔍 Optical Zoom Support
            val zoomRange =
                characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
            if (zoomRange != null && zoomRange > 1.0f) {
                supportsOpticalZoom = "✅ Supported (Max Zoom: ${zoomRange}x)"
            }

            // 🌄 Ultra-Wide Camera Support
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val focalLengths =
                characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)

            if (lensFacing == CameraCharacteristics.LENS_FACING_BACK && focalLengths != null) {
                val minFocalLength = focalLengths.minOrNull()
                if (minFocalLength != null && minFocalLength < 2.0f) {
                    supportsUltraWide = "✅ Supported (Focal Length: $minFocalLength mm)"
                }
            }
        }
    } catch (e: Exception) {
        handleError(e)
        supportsOpticalZoom = "⚠️ Unable to Detect"
        supportsUltraWide = "⚠️ Unable to Detect"
    }

    return """
        📷 Back Camera: ${if (hasBackCamera) "✅ Available" else "❌ Not Found"}
        
        🤳 Front Camera: ${if (hasFrontCamera) "✅ Available" else "❌ Not Found"}
        
        🔍 Optical Zoom: $supportsOpticalZoom
        
        🌄 Ultra-Wide Camera: $supportsUltraWide
        
        🔄 Autofocus: ${if (supportsAutofocus) "✅ Supported" else "❌ No Autofocus"}
        
        👤 Face Detection: ${if (supportsFaceDetection) "✅ Supported" else "❌ No Face Detection"}

        🎤 Microphone: ${if (hasMic) "✅ Available" else "❌ Not Found"}
        
        🔇 Noise Cancellation: ${if (supportsNoiseCancellation) "✅ Supported" else "❌ Not Available"}
        
        🔁 Echo Cancellation: ${if (supportsEchoCancellation) "✅ Supported" else "❌ Not Available"}

        🔊 Speaker: ${if (hasSpeaker) "✅ Enabled" else "❌ Disabled"}
        
        🎶 Stereo Speakers: ${if (supportsStereoSpeakers) "✅ Supported" else "❌ No Stereo Audio"}
        
        🎧 External Audio Output: ${if (hasExternalAudioOutput) "✅ Supported" else "❌ No External Output"}

        🔦 Flashlight: ${if (hasFlash) "✅ Available" else "❌ Not Found"}
        
        🔥 Torch Mode: ${if (supportsTorchMode) "✅ Supported" else "❌ No Torch Mode"}

        🔒 Active Mic/Camera Usage: $micCamStatus
        
        🔍 Recent Mic/Camera Usage Log:
        ${getRecentCameraMicUsageLog()}
    """.trimIndent()
}


fun isCameraOrMicActive(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // API 29+
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val micAccess = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_RECORD_AUDIO, android.os.Process.myUid(), context.packageName
        )
        val camAccess = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_CAMERA, android.os.Process.myUid(), context.packageName
        )

        return when {
            micAccess == AppOpsManager.MODE_ALLOWED || camAccess == AppOpsManager.MODE_ALLOWED -> "🎤🎥 Mic/Camera Active!"
            micAccess == AppOpsManager.MODE_ERRORED && camAccess == AppOpsManager.MODE_ERRORED -> "✅ Mic & Camera Inactive"
            else -> "Unknown"
        }
    } else {
        return "Unsupported (Requires Android 10+)"
    }
}


fun getAvailableStorage(): String {
    return try {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val availableBytes = stat.availableBytes
        val totalBytes = stat.totalBytes
        val availableGB = availableBytes / (1024 * 1024 * 1024) // Convert to GB
        val totalGB = totalBytes / (1024 * 1024 * 1024) // Convert to GB
        "$availableGB GB / $totalGB GB"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getSupportedMediaFormats(): String {
    val audioFormats = mutableSetOf<String>()
    val videoFormats = mutableSetOf<String>()
    val imageFormats = listOf("jpeg", "png", "webp", "gif", "bmp", "svg") // Common image types
    val documentFormats = listOf("pdf", "docx", "xlsx", "pptx", "txt", "html", "json", "csv")

    try {
        val codecs = MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos
        for (codec in codecs) {
            if (!codec.isEncoder) continue
            codec.supportedTypes.forEach { type ->
                when {
                    type.contains("audio", ignoreCase = true) -> audioFormats.add(type)
                    type.contains("video", ignoreCase = true) -> videoFormats.add(type)
                }
            }
        }
    } catch (e: Exception) {
        handleError(e)
        audioFormats.addAll(listOf("audio/mpeg", "audio/aac", "audio/wav"))
        videoFormats.addAll(listOf("video/mp4", "video/3gp", "video/webm"))
    }

    return """
        🎵 Audio : ${audioFormats.joinToString(", ") { it.removePrefix("audio/") }}
        
        🎬 Video : ${videoFormats.joinToString(", ") { it.removePrefix("video/") }}
        
        🖼️ Image : ${imageFormats.joinToString(", ")}
        
        📄 Document : ${documentFormats.joinToString(", ")}
    """.trimIndent()
}

fun getSensorList(context: Context): List<String> {
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

    return sensors.map { sensor ->
        """ 
        𐂷 Sensor Name: ${sensor.name}
        📌 Type: ${sensor.stringType}
        🏭 Vendor: ${sensor.vendor} 
        🔢 Version: ${sensor.version}
        🔋 Power Usage: ${sensor.power} mA
        🎯 Resolution: ${sensor.resolution}
        📏 Max Range: ${sensor.maximumRange} 
        ⏳ Max Delay: ${sensor.maxDelay} μs
        """.trimIndent()
    }
}

fun isPlayStoreCertified(context: Context): String {
    return try {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo("com.android.vending", 0)
        if (packageInfo != null) "Certified" else "Not Certified"
    } catch (e: Exception) {
        handleError(e)
        "Not Certified"
    }
}


fun getDisplayInfo(context: Context): Map<String, String> {
    val displayMetrics = context.resources.displayMetrics
    val windowManager: WindowManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.getSystemService(WindowManager::class.java)
    } else {
        context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        windowManager?.defaultDisplay
    }
    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels
    val density = displayMetrics.densityDpi
    val refreshRate = display?.refreshRate ?: 0f
    val isHdrSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        display?.isHdr ?: false
    } else {
        false
    }
    val screenSize =
        String.format(Locale.getDefault(), "%.1f inches", calculateScreenSize(displayMetrics))

    return mapOf(
        "📺 Screen Resolution" to "${screenWidth}x${screenHeight}",

        "📏 Screen Size" to screenSize,

        "🔄 Refresh Rate" to "$refreshRate Hz",

        "🖥️ Density" to "$density dpi",

        "🔆 HDR Support" to if (isHdrSupported) "Yes" else "No",

        "🖐 Touch Latency" to measureTouchLatency(),

        "🌗 Dark Mode Status" to isDarkModeEnabled(context),
    )
}

// Helper function to calculate screen size
fun calculateScreenSize(metrics: DisplayMetrics): Float {
    val widthInches = metrics.widthPixels / metrics.xdpi
    val heightInches = metrics.heightPixels / metrics.ydpi
    return sqrt((widthInches * widthInches) + (heightInches * heightInches))
}


fun measureTouchLatency(): String {
    return try {
        val startTime = System.nanoTime()
        Thread.sleep(5) // Simulate a minor delay
        val endTime = System.nanoTime()
        val latency = (endTime - startTime) / 1_000_000 // Convert to ms
        "$latency ms"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getAvailableFonts(): String {
    return try {
        val fontsDir = File("/system/fonts")
        if (fontsDir.exists() && fontsDir.isDirectory) {
            fontsDir.listFiles()?.joinToString(",\n") { it.nameWithoutExtension }
                ?: "No Fonts Found"
        } else {
            "No Access"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}

fun isLargeTextEnabled(context: Context): String {
    return try {
        val configuration = context.resources.configuration
        if (configuration.fontScale > 1.0) "Enabled" else "Disabled"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getFontScale(context: Context): String {
    return try {
        val configuration = context.resources.configuration
        "x${configuration.fontScale}"
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }
}


fun getFontInfo(context: Context): String {
    val availableFonts = getAvailableFonts()
    val fontScale = getFontScale(context)
    val largeTextStatus = isLargeTextEnabled(context)
    return "\uD83D\uDD0D Font Scale: $fontScale\n" + "\n" + "\uD83D\uDD20 Large Text: $largeTextStatus\n 🔤 System Fonts: $availableFonts \n\n".trimIndent()
}


fun getDisplayInfoString(context: Context): String {
    val displayInfo = getDisplayInfo(context)
    return displayInfo.entries.joinToString("\n\n") { "${it.key}: ${it.value}" }
}

/**
 * ⚠️ IMPORTANT: This is an ESTIMATE, not real cycle count data!
 * Android does NOT provide battery cycle count through standard APIs.
 * This is a rough approximation based on battery capacity percentage.
 * Real cycle count is only available through manufacturer-specific APIs or root access.
 * 
 * @param context Android context
 * @return Estimated cycle count string with clear disclaimer
 */
fun getBatteryCycleEstimate(context: Context): String {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    
    // ⚠️ This is a ROUGH ESTIMATE based on capacity, NOT real cycle count
    // Formula assumes: new battery = 100% capacity, degraded = lower capacity
    // This is NOT accurate - capacity can vary for many reasons (temperature, age, usage patterns)
    val estimatedCycles = (1000 - capacity) / 10
    
    return "🔋 Estimated Cycles (Approximation): ~$estimatedCycles cycles"
}


fun getTelephonyInfo(context: Context): String {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager

    if (telephonyManager == null) {
        return "❌ Telephony Manager Not Available"
    }

    // ✅ Get SIM Slot Count
    val simSlotCount = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            telephonyManager.activeModemCount.toString()
        } else {
            telephonyManager.phoneCount.toString()
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }


    // ✅ Get SIM Information
    val simInfo = if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        try {
            val activeSubscriptionInfoList =
                subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
            if (activeSubscriptionInfoList.isNotEmpty()) {
                activeSubscriptionInfoList.joinToString("\n") { subInfo ->
                    """
                    📲 SIM Slot: ${subInfo.simSlotIndex + 1}       
                    🏷 Carrier: ${subInfo.carrierName ?: "Unknown"}
                    📡 Country: ${subInfo.countryIso.uppercase(Locale.getDefault())}           
                    🔄 Roaming: ${if (subInfo.dataRoaming == SubscriptionManager.DATA_ROAMING_ENABLE) "Enabled" else "Disabled"}                  
                    📶 MCC-MNC: ${subInfo.mcc}-${subInfo.mnc}
                    
                    """.trimIndent()
                }
            } else {
                "No SIM Found"
            }
        } catch (e: Exception) {
            handleError(e)
            "Error Fetching SIM Info"
        }
    } else {
        "Permission Required"
    }

    // ✅ Get Network Type
    val networkType = try {
        when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_HSPA -> "3G HSPA"
            TelephonyManager.NETWORK_TYPE_EDGE -> "2G EDGE"
            TelephonyManager.NETWORK_TYPE_GPRS -> "2G GPRS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO (3G)"

            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS (3G)"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA (3G)"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA (3G)"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+ (3G)"
            TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN (2G)"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT (2G)"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "Unknown"
            else -> "Unavailable"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }

    // ✅ Get SIM State
    val simState = try {
        when (telephonyManager.simState) {
            TelephonyManager.SIM_STATE_READY -> "✅ Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "❌ No SIM"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "🔒 PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "🔐 PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "🔓 Network Locked"
            else -> "Unknown"
        }
    } catch (e: Exception) {
        handleError(e)
        "Unavailable"
    }

    return """
📶 SIM Slots Available: $simSlotCount
        
🔗 Network Type: $networkType
    
🛜 SIM State: $simState
    
🛜 SIM Info =>

$simInfo
    """.trimIndent()
}


fun isTimeAutoSyncEnabled(context: Context): String {
    return if (Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AUTO_TIME,
            0
        ) == 1
    ) "✅ Auto Sync Enabled" else "⚠️ Manual Time (Check Settings)"
}

fun isDarkModeEnabled(context: Context): String {
    val nightModeFlags =
        context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) "🌙 Dark Mode Enabled" else "☀️ Light Mode"
}


fun getLastLogs(): String {
    return try {
        val process = Runtime.getRuntime().exec("logcat -t 10")
        process.inputStream.bufferedReader().readLines().joinToString("\n")
    } catch (e: Exception) {
        handleError(e)
        "Logcat access restricted"
    }
}

fun isDeviceBeingMonitored(context: Context): String {
    val hasScreenRecordingApps = detectScreenRecordingApps(context)
    val suspiciousAccessibilityServices = detectSuspiciousAccessibilityServices(context)
    val adbEnabled = isUsbDebuggingEnabled(context)
    val unknownCerts = checkSSLCertificateHijack()
    val deepPacketInspection = checkDPIDetection()
    val appPermissionsMisuse = detectDangerousPermissions(context)
    val backgroundMicUsage = isMicrophoneBeingUsed(context)
    val unusualBatteryDrain = detectBatteryDrain()
    val keyloggerCheck = detectKeylogger(context)

    return """        
🎥 Screen Recording Apps Detected: $hasScreenRecordingApps
        
🕵️ Suspicious Accessibility Services (Spying Apps): $suspiciousAccessibilityServices
       
🖥️ Developer Mode / Remote Debugging (ADB Enabled): $adbEnabled
       
🔒 Fake Security Certificates (SSL Hijack by ISP/Government): $unknownCerts
       
📡 Deep Packet Inspection (ISP/Government Scanning Traffic): $deepPacketInspection
      
📲 Apps with Excessive Permissions (Camera, Mic, SMS, Call Logs): $appPermissionsMisuse
       
🎙 Background Microphone Usage (Eavesdropping Risk): $backgroundMicUsage
       
🔋 Unusual Battery Drain (Spyware Running in Background): $unusualBatteryDrain
       
⌨️ Keylogger Detection (Silent Keyboard Tracking Apps): $keyloggerCheck
    """.trimIndent()
}

/** ⌨️ Detect Keylogger Apps */
fun detectKeylogger(context: Context): String {
    val keyloggerApps = listOf(
        "com.android.keylogger",
        "com.refog.keylogger",
        "com.spy.keylogger",
        "com.mocana.keylogger",
        "com.km.keylogger",
        "com.smart.keylogger",
        "com.keylogger.recorder",
        "com.advanced.keylogger",
        "com.secretlogger.spy",
        "com.keystroke.logger",
        "com.silent.keylogger",
        "com.hiddenlogger.spy",
        "com.spyware.keylogger"
    )

    val installedApps = getInstalledApps(context)
    val detectedKeyloggers = keyloggerApps.filter { installedApps.contains(it) }
    return if (detectedKeyloggers.isNotEmpty()) "Keyloggers Detected: ${detectedKeyloggers.joinToString()}" else "No Keyloggers Found"
}


/** 🎥 Detect Screen Recording Apps */
fun detectScreenRecordingApps(context: Context): String {
    context.packageManager
    val knownScreenRecorders = listOf(
        "com.android.systemui.screenrecord",
        "com.duapps.recorder",
        "com.mobzapp.recme",
        "com.kimcy929.screenrecorder",
        "com.hecorat.screenrecorder.free",
        "com.iwobanas.screenrecorder",
        "com.recorder.hidden",
        "com.nll.screenrecorder",
        "com.axndx.screenrecorder",
        "com.apowersoft.screenrecorder",
        "com.vidma.screenrecorder",
        "com.hidden.screenrecorder",
        "com.spy.screenrecorder"
    )


    val installedApps = getInstalledApps(context)
    val detectedApps = knownScreenRecorders.filter { installedApps.contains(it) }

    return if (detectedApps.isNotEmpty()) "Found: ${detectedApps.joinToString()}" else "None Detected"
}

/** 📲 Detect Apps Misusing Dangerous Permissions */
fun detectDangerousPermissions(context: Context): String {
    val dangerousPermissions = listOf(
        Manifest.permission.READ_SMS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    context.packageManager
    val installedApps = getInstalledApps(context) // ✅ Uses the updated installed apps method
    val appsWithPermissions = installedApps.filter { _ ->
        dangerousPermissions.any { permission ->
            ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    return if (appsWithPermissions.isNotEmpty()) {
        "🚨 Apps Using Sensitive Permissions:\n${appsWithPermissions.joinToString("\n")}"
    } else {
        "✅ No Apps Misusing Permissions"
    }
}


fun getInstalledApps(context: Context): List<String> {
    val packageManager = context.packageManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // **Android 11+ (Package Visibility Restricted)**
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedApps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        resolvedApps.map { it.activityInfo.packageName }
    } else {
        // **Android 10 and Below (Full Access)**
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map { it.packageName }
    }
}


/** 🕵️ Detect Suspicious Accessibility Services */
fun detectSuspiciousAccessibilityServices(context: Context): String {
    val settings = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return if (!settings.isNullOrBlank()) "Suspicious Services Found" else "No Suspicious Services"
}


/** 🎙 Detect Microphone Usage in Background */
fun isMicrophoneBeingUsed(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val micAccess = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_RECORD_AUDIO, android.os.Process.myUid(), context.packageName
        )
        if (micAccess == AppOpsManager.MODE_ALLOWED) "Active" else "Not Active"
    } else {
        "Unsupported on this Android Version"
    }
}

/** 🔋 Detect Unusual Battery Drain */
fun detectBatteryDrain(): String {
    return try {
        val process = Runtime.getRuntime().exec("dumpsys batterystats")
        val output = process.inputStream.bufferedReader().readText()
        val highDrain = Regex("top=\\[(.*?)\\]").find(output)?.groupValues?.get(1)
        if (!highDrain.isNullOrEmpty()) "High Usage Apps: $highDrain" else "Normal Battery Consumption"
    } catch (e: Exception) {
        handleError(e)
        "Unknown"
    }
}

fun getAiInferenceSupport(context: Context): String {
    val hasNNAPI = context.packageManager.hasSystemFeature("android.hardware.neuralnetworks")
    val ramInfo = getRamUsage(context)
    val supportedAbis = Build.SUPPORTED_64_BIT_ABIS.joinToString()
    val is64bit = supportedAbis.contains("arm64") || supportedAbis.contains("x86_64")
    val ramOK = !ramInfo.contains("MB") || ramInfo.contains("4000") || ramInfo.contains("6000")

    val readiness = if (hasNNAPI && ramOK && is64bit) {
        "✅ Your phone is AI-ready. Can run small local models."
    } else {
        "⚠️ Not fully AI-ready. May struggle with large models or neural tasks."
    }

    return """
    🤖 Neural Network Acceleration: ${if (hasNNAPI) "✅ Supported" else "❌ Not Supported"}
    
    📦 RAM Info: $ramInfo
    
    🔢 64-bit CPU Support: ${if (is64bit) "✅ Yes" else "❌ No"}
    
    🧠 AI Readiness Summary:
    $readiness
    """.trimIndent()
}



/**
 * Gets thermal zone temperatures from system files (requires root on most devices)
 * Falls back to BatteryManager API for battery temperature (works on all devices)
 * @param context Context to access BatteryManager API for fallback
 * @return Formatted string with thermal data, or battery temperature if thermal zones unavailable
 */
fun getThermalZoneTemperatures(context: Context? = null): String {
    val basePath = "/sys/class/thermal"
    val thermalData = StringBuilder()
    var hasThermalData = false
    
    // Try to read thermal zones (works on rooted devices or some custom ROMs)
    try {
        val dir = File(basePath)
        dir.listFiles()?.filter { it.name.startsWith("thermal_zone") }?.forEach { zone ->
            try {
                val type = File(zone, "type").readText().trim()
                val temp = File(zone, "temp").readText().trim().toFloat() / 1000
                thermalData.append("🌡️ $type: ${"%.1f".format(temp)}°C\n")
                hasThermalData = true
            } catch (_: Exception) {
            }
        }
    } catch (e: Exception) {
        handleError(e)
    }
    
    // If no thermal zone data found, try BatteryManager API (works on all devices)
    if (!hasThermalData && context != null) {
        val batteryTemp = getBatteryTemperature(context)
        if (batteryTemp != null) {
            thermalData.append("🔋 Battery: ${"%.1f".format(batteryTemp)}°C\n")
            hasThermalData = true
        }
    }
    
    // If still no data, return appropriate message
    return if (hasThermalData) {
        thermalData.toString()
    } else if (context != null) {
        "❌ No thermal data available. Thermal zones require root access on most devices."
    } else {
        "❌ No thermal data available. Pass Context parameter to enable BatteryManager fallback."
    }
}

/**
 * Gets battery temperature using BatteryManager API (works on all devices without root)
 * @return Battery temperature in Celsius, or null if unavailable
 */
fun getBatteryTemperature(context: Context): Float? {
    return try {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val tempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        if (tempRaw > 0) {
            tempRaw / 10.0f // Convert from tenths of degree to degree
        } else {
            null
        }
    } catch (e: Exception) {
        handleError(e)
        null
    }
}

/**
 * Gets thermal state using PowerManager API (Android 10+)
 * @return Thermal state string: "NORMAL", "LIGHT", "MODERATE", "HOT", "CRITICAL", "OVERHEAT", or "UNKNOWN"
 */
fun getThermalState(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_LIGHT -> "LIGHT"
                PowerManager.THERMAL_STATUS_MODERATE -> "MODERATE"
                PowerManager.THERMAL_STATUS_SEVERE -> "HOT"
                PowerManager.THERMAL_STATUS_CRITICAL -> "CRITICAL"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "OVERHEAT"
                else -> "NORMAL"
            }
        } catch (e: Exception) {
            handleError(e)
            "UNKNOWN"
        }
    } else {
        // Fallback to parsing from power state string for older Android versions
        val powerState = getCompactPowerState(context)
        extractThermalStateFromPowerState(powerState)
    }
}

/**
 * Extracts temperature value from thermal status string by type (CPU, Battery, GPU, etc.)
 * @param thermalStatus The thermal status string from getThermalZoneTemperatures()
 * @param type The type to extract (e.g., "CPU", "Battery", "GPU")
 * @return Temperature in Celsius, or null if not found
 */
fun extractTemperature(thermalStatus: String, type: String): Float? {
    val pattern = java.util.regex.Pattern.compile("$type[^:]*:\\s*([\\d.]+)°C", java.util.regex.Pattern.CASE_INSENSITIVE)
    val matcher = pattern.matcher(thermalStatus)
    return if (matcher.find()) {
        matcher.group(1)?.toFloatOrNull()
    } else null
}

/**
 * Extracts all temperatures from thermal status string
 * @param thermalStatus The thermal status string from getThermalZoneTemperatures()
 * @return Map of temperature type to value in Celsius
 */
fun extractAllTemperatures(thermalStatus: String): Map<String, Float> {
    val temps = mutableMapOf<String, Float>()
    val pattern = java.util.regex.Pattern.compile("([^:]+):\\s*([\\d.]+)°C")
    val matcher = pattern.matcher(thermalStatus)
    while (matcher.find()) {
        val name = matcher.group(1)?.trim()?.replace("🌡️", "")?.trim() ?: ""
        val temp = matcher.group(2)?.toFloatOrNull()
        if (temp != null && name.isNotEmpty()) {
            temps[name] = temp
        }
    }
    return temps
}

/**
 * Extracts thermal state from power state string
 * @param powerState The power state string from getCompactPowerState()
 * @return Thermal state string: "NORMAL", "LIGHT", "MODERATE", "HOT", "CRITICAL", "OVERHEAT", or "UNKNOWN"
 */
fun extractThermalStateFromPowerState(powerState: String): String {
    return when {
        powerState.contains("Overheat", ignoreCase = true) -> "OVERHEAT"
        powerState.contains("Critical", ignoreCase = true) -> "CRITICAL"
        powerState.contains("Hot", ignoreCase = true) -> "HOT"
        powerState.contains("Moderate", ignoreCase = true) -> "MODERATE"
        powerState.contains("Light", ignoreCase = true) -> "LIGHT"
        powerState.contains("Normal", ignoreCase = true) -> "NORMAL"
        else -> "UNKNOWN"
    }
}

fun detectSensorSpoofing(context: Context): String {
    val knownSpoofApps = listOf(
        "com.fakegps.mock",
        "com.lexa.fakegps",
        "com.incorporateapps.fakegps.fre",
        "com.just4freak.mocklocation"
    )
    val installedApps = getInstalledApps(context)
    val spoofers = knownSpoofApps.filter { installedApps.contains(it) }

    return if (spoofers.isNotEmpty()) {
        "🚫 Sensor Spoofing Detected: ${spoofers.joinToString()}"
    } else {
        "✅ No GPS or sensor spoofing apps found."
    }
}

fun detectHiddenApps(context: Context): String {
    val apps = context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    val hiddenApps = apps.filter {
        (it.flags and ApplicationInfo.FLAG_SYSTEM == 0) && it.loadLabel(context.packageManager)
            .isNullOrEmpty()
    }

    return if (hiddenApps.isNotEmpty()) {
        "🔍 Hidden apps found: ${hiddenApps.joinToString { it.packageName }}"
    } else {
        "✅ No hidden or stealth apps detected."
    }
}

fun getPhoneHackabilityScore(context: Context): String {
    val root = isDeviceRooted()
    val usb = isUsbDebuggingEnabled(context)
    val selinux = getSecurityInfo(context)
    val score = listOf(root, usb, selinux).count { it.contains("Yes") || it.contains("Enabled") }

    val rating = when (score) {
        0 -> "🔐 Secure"
        1 -> "⚠️ Mild Risk"
        2 -> "🚨 High Risk"
        else -> "☠️ Critical Risk"
    }

    return "📊 Hackability Score: $rating ($score/3 vulnerabilities found)"
}

fun getFaceUnlockTrustLevel(context: Context): String {
    val hasBiometric = context.packageManager.hasSystemFeature(PackageManager.FEATURE_FACE)
    val level = if (hasBiometric) {
        "🧠 Likely 2D face unlock (camera-based). Can be fooled by a photo."
    } else {
        "✅ No face unlock detected or hardware-based. Safer."
    }
    return level
}

fun detectAiVoiceCloneRisk(context: Context): String {
    val micStatus = isMicrophoneBeingUsed(context)
    return when (micStatus) {
        "Active" -> "🎙️ Microphone is currently in use. Potential AI voice clone risk."
        "Not Active" -> "✅ No active mic usage. Safe from voice cloning."
        else -> "⚠️ Unable to determine mic status."
    }
}

fun detectAdTrackingApps(context: Context): String {
    val knownAdSDKs = listOf("com.google.ads", "com.facebook.ads", "com.mopub", "com.unity3d.ads")
    val installed = getInstalledApps(context)
    val suspectApps = installed.filter { pkg ->
        knownAdSDKs.any { pkg.contains(it) }
    }

    return if (suspectApps.isNotEmpty()) {
        "👁️ Ad SDKs found in ${suspectApps.size} apps:\n" + suspectApps.joinToString("\n")
    } else {
        "✅ No major ad tracking SDKs found in your apps."
    }
}

fun detectMotionWhileLocked(context: Context): String {
    val accelFile = File("/sys/class/input") // Simulated for illustration
    val hasSensorActivity =
        accelFile.exists() // Placeholder: replace with actual sensor log if you store it

    return if (hasSensorActivity) {
        "👣 Your phone moved while locked — possible snooping at night or while away."
    } else {
        "✅ No motion detected while locked."
    }
}

fun getRecentCameraMicUsageLog(): String {
    return try {
        val logcat = Runtime.getRuntime().exec("logcat -d -t 100")
            .inputStream.bufferedReader().readLines()

        val matches = logcat.filter {
            it.contains("CameraService") || it.contains("AudioRecord") || it.contains("mediarecorder")
        }

        if (matches.isNotEmpty()) {
            "🔍 Recent usage detected:\n" + matches.takeLast(5).joinToString("\n")
        } else {
            "✅ No recent mic or camera access in logs."
        }
    } catch (e: Exception) {
        handleError(e)
        "❌ Unable to read recent mic/camera usage"
    }
}
















