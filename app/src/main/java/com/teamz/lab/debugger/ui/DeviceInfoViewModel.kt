package com.teamz.lab.debugger.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teamz.lab.debugger.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

/**
 * ViewModel for Device Info data
 * Manages batched loading with progressive delays to prevent system overload
 * Reduces recompositions from 25+ to 3 (one per batch)
 */
@Stable
data class DeviceInfoState(
    // Critical info (load first - Batch 1)
    val deviceDetails: String = "",
    val cpuDetails: String = "",
    val memoryStorage: String = "",
    val batteryInfo: String = "",
    val gpsInfo: String = "",
    
    // Secondary info (load after 500ms - Batch 2)
    val thermalStatus: String = "",
    val cameraInfo: String = "",
    val sensorList: String = "",
    val displayInfo: String = "",
    val telephonyInfo: String = "",
    val dateTimeInfo: String = "",
    
    // Tertiary info (load after 1000ms - Batch 3)
    val recentLogs: String = "",
    val fontInfo: String = "",
    val fileFormat: String = "",
    val securityInfo: String = "",
    val rootStatus: String = "",
    val usbDebugging: String = "",
    val aiInferenceSupport: String = "",
    val thermalZoneInfo: String = "",
    val spoofingStatus: String = "",
    val hiddenAppsStatus: String = "",
    val voiceCloneRisk: String = "",
    val hackability: String = "",
    val faceUnlockTrust: String = "",
    val adTracking: String = "",
    val isDeviceBeingMonitored: String = "",
    
    // FPS info (main thread - handled separately)
    val frameRate: String = "",
    val frameDropData: String = "",
    val gpuDetails: String = "",
    
    // Loading states
    val isLoadingCritical: Boolean = true,
    val isLoadingSecondary: Boolean = false,
    val isLoadingTertiary: Boolean = false,
    val isFullyLoaded: Boolean = false
)

class DeviceInfoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _state = MutableStateFlow(DeviceInfoState())
    val state: StateFlow<DeviceInfoState> = _state.asStateFlow()
    
    // Semaphore to limit concurrent coroutines to 5 max (prevents system overload)
    private val semaphore = Semaphore(5)
    
    /**
     * Load all device info with progressive batching
     * Batch 1: Critical info (immediate)
     * Batch 2: Secondary info (after 500ms)
     * Batch 3: Tertiary info (after 1000ms)
     */
    fun loadDeviceInfo(context: Context) {
        android.util.Log.d("DeviceInfoViewModel", "🚀 Starting device info loading...")
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                
                // Batch 1: Critical info (load immediately)
                android.util.Log.d("DeviceInfoViewModel", "📦 Loading Batch 1 (Critical)...")
                loadCriticalInfo(context)
                val batch1Time = System.currentTimeMillis() - startTime
                android.util.Log.d("DeviceInfoViewModel", "✅ Batch 1 complete in ${batch1Time}ms")
                
                // Batch 2: Secondary info (after 500ms delay)
                delay(500)
                val batch2Start = System.currentTimeMillis()
                android.util.Log.d("DeviceInfoViewModel", "📦 Loading Batch 2 (Secondary)...")
                loadSecondaryInfo(context)
                val batch2Time = System.currentTimeMillis() - batch2Start
                android.util.Log.d("DeviceInfoViewModel", "✅ Batch 2 complete in ${batch2Time}ms")
                
                // Batch 3: Tertiary info (after 1000ms delay)
                delay(500)
                val batch3Start = System.currentTimeMillis()
                android.util.Log.d("DeviceInfoViewModel", "📦 Loading Batch 3 (Tertiary)...")
                loadTertiaryInfo(context)
                val batch3Time = System.currentTimeMillis() - batch3Start
                android.util.Log.d("DeviceInfoViewModel", "✅ Batch 3 complete in ${batch3Time}ms")
                
                // Wait a bit for FPS and GPU to load (they're loaded via callbacks)
                // Give them 3 seconds max, then mark as fully loaded regardless
                android.util.Log.d("DeviceInfoViewModel", "⏳ Waiting for FPS/GPU callbacks (max 3s)...")
                delay(3000)
                
                val totalTime = System.currentTimeMillis() - startTime
                // Mark as fully loaded - all data should be ready now
                android.util.Log.d("DeviceInfoViewModel", "🎉 All batches complete in ${totalTime}ms - marking as fully loaded")
                _state.update { it.copy(isFullyLoaded = true) }
                android.util.Log.d("DeviceInfoViewModel", "✅ isFullyLoaded = true")
                
                // Log final state for debugging
                val finalState = _state.value
                android.util.Log.d("DeviceInfoViewModel", "📊 Final State Check:")
                android.util.Log.d("DeviceInfoViewModel", "  - deviceDetails: ${if (finalState.deviceDetails.isNotEmpty()) "✅" else "❌"} (${finalState.deviceDetails.length} chars)")
                android.util.Log.d("DeviceInfoViewModel", "  - cpuDetails: ${if (finalState.cpuDetails.isNotEmpty()) "✅" else "❌"} (${finalState.cpuDetails.length} chars)")
                android.util.Log.d("DeviceInfoViewModel", "  - frameRate: ${if (finalState.frameRate.isNotEmpty()) "✅" else "❌"} (${finalState.frameRate})")
                android.util.Log.d("DeviceInfoViewModel", "  - gpuDetails: ${if (finalState.gpuDetails.isNotEmpty()) "✅" else "❌"} (${finalState.gpuDetails.length} chars)")
            } catch (e: Exception) {
                android.util.Log.e("DeviceInfoViewModel", "❌ Fatal error in loadDeviceInfo", e)
                // Even on error, mark as loaded so UI doesn't hang
                _state.update { it.copy(isFullyLoaded = true) }
            }
        }
    }
    
    /**
     * Load critical device info (Batch 1)
     * These are the most important fields that users see first
     */
    private suspend fun loadCriticalInfo(context: Context) {
        _state.update { it.copy(isLoadingCritical = true) }
        
        coroutineScope {
            val results = listOf(
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getDeviceInfoString(context)
                        android.util.Log.d("DeviceInfoViewModel", "✅ Loaded deviceDetails: ${result.take(50)}...")
                        result.ifEmpty { "No device information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading deviceDetails", e)
                        "Error loading device information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getCpuInfo()
                        android.util.Log.d("DeviceInfoViewModel", "✅ Loaded cpuDetails: ${result.take(50)}...")
                        result.ifEmpty { "No CPU information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading cpuDetails", e)
                        "Error loading CPU information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getMemoryAndStorageInfo(context)
                        android.util.Log.d("DeviceInfoViewModel", "✅ Loaded memoryStorage: ${result.take(50)}...")
                        result.ifEmpty { "No memory information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading memoryStorage", e)
                        "Error loading memory information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getBatteryChargingInfo(context)
                        android.util.Log.d("DeviceInfoViewModel", "✅ Loaded batteryInfo: ${result.take(50)}...")
                        result.ifEmpty { "No battery information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading batteryInfo", e)
                        "Error loading battery information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getLocationAndGPSInfoAsync(context)
                        android.util.Log.d("DeviceInfoViewModel", "✅ Loaded gpsInfo: ${result.take(50)}...")
                        result.ifEmpty { "No GPS information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading gpsInfo", e)
                        "Error loading GPS information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                }
            ).awaitAll()
            
            // Single batched update - only 1 recomposition!
            android.util.Log.d("DeviceInfoViewModel", "✅ Batch 1 complete - updating state")
            _state.update {
                val newState = it.copy(
                    deviceDetails = results[0],
                    cpuDetails = results[1],
                    memoryStorage = results[2],
                    batteryInfo = results[3],
                    gpsInfo = results[4],
                    isLoadingCritical = false
                )
                android.util.Log.d("DeviceInfoViewModel", "✅ Batch 1 state updated - deviceDetails: ${newState.deviceDetails.take(50)}...")
                newState
            }
        }
    }
    
    /**
     * Load secondary device info (Batch 2)
     * These are important but not critical for initial display
     */
    private suspend fun loadSecondaryInfo(context: Context) {
        _state.update { it.copy(isLoadingSecondary = true) }
        
        coroutineScope {
            val results = listOf(
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getThermalZoneTemperatures(context)
                        result.ifEmpty { "No thermal information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading thermalStatus", e)
                        "Error loading thermal information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getCameraMicSpeakerFlashInfo(context)
                        result.ifEmpty { "No camera information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading cameraInfo", e)
                        "Error loading camera information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getSensorList(context).joinToString("\n\n")
                        result.ifEmpty { "No Sensors Found" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading sensorList", e)
                        "Error loading sensor information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getDisplayInfoString(context)
                        result.ifEmpty { "No display information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading displayInfo", e)
                        "Error loading display information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getTelephonyInfo(context)
                        result.ifEmpty { "No telephony information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading telephonyInfo", e)
                        "Error loading telephony information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getDateTimeInfo(context)
                        result.ifEmpty { "No date/time information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading dateTimeInfo", e)
                        "Error loading date/time information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                }
            ).awaitAll()
            
            // Single batched update - only 1 recomposition!
            android.util.Log.d("DeviceInfoViewModel", "✅ Batch 2 complete - updating state")
            _state.update {
                it.copy(
                    thermalStatus = results[0],
                    cameraInfo = results[1],
                    sensorList = results[2],
                    displayInfo = results[3],
                    telephonyInfo = results[4],
                    dateTimeInfo = results[5],
                    isLoadingSecondary = false
                )
            }
            android.util.Log.d("DeviceInfoViewModel", "✅ Batch 2 state updated")
        }
    }
    
    /**
     * Load tertiary device info (Batch 3)
     * These are less critical and can load last
     */
    private suspend fun loadTertiaryInfo(context: Context) {
        _state.update { it.copy(isLoadingTertiary = true) }
        
        coroutineScope {
            val results = listOf(
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getLastLogs()
                        result.ifEmpty { "No recent logs available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading recentLogs", e)
                        "Error loading logs: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getFontInfo(context)
                        result.ifEmpty { "No font information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading fontInfo", e)
                        "Error loading font information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getSupportedMediaFormats()
                        result.ifEmpty { "No media format information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading fileFormat", e)
                        "Error loading media format information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getSecurityInfo(context)
                        result.ifEmpty { "No security information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading securityInfo", e)
                        "Error loading security information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = isDeviceRooted()
                        result.ifEmpty { "Unable to determine root status" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading rootStatus", e)
                        "Error checking root status: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = isUsbDebuggingEnabled(context)
                        result.ifEmpty { "Unable to determine USB debugging status" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading usbDebugging", e)
                        "Error checking USB debugging: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getAiInferenceSupport(context)
                        result.ifEmpty { "No AI inference information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading aiInferenceSupport", e)
                        "Error loading AI inference information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getThermalZoneTemperatures(context)
                        result.ifEmpty { "No thermal zone information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading thermalZoneInfo", e)
                        "Error loading thermal zone information: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = detectSensorSpoofing(context)
                        result.ifEmpty { "No sensor spoofing information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading spoofingStatus", e)
                        "Error checking sensor spoofing: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = detectHiddenApps(context)
                        result.ifEmpty { "No hidden apps information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading hiddenAppsStatus", e)
                        "Error checking hidden apps: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getPhoneHackabilityScore(context)
                        result.ifEmpty { "No hackability information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading hackability", e)
                        "Error calculating hackability: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = getFaceUnlockTrustLevel(context)
                        result.ifEmpty { "No face unlock information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading faceUnlockTrust", e)
                        "Error checking face unlock: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = detectAiVoiceCloneRisk(context)
                        result.ifEmpty { "No voice clone risk information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading voiceCloneRisk", e)
                        "Error checking voice clone risk: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = detectAdTrackingApps(context)
                        result.ifEmpty { "No ad tracking information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading adTracking", e)
                        "Error checking ad tracking: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                },
                async(Dispatchers.IO) {
                    semaphore.acquire()
                    try {
                        val result = isDeviceBeingMonitored(context)
                        result.ifEmpty { "No monitoring information available" }
                    } catch (e: Exception) {
                        android.util.Log.e("DeviceInfoViewModel", "❌ Error loading isDeviceBeingMonitored", e)
                        "Error checking device monitoring: ${e.message}"
                    } finally {
                        semaphore.release()
                    }
                }
            ).awaitAll()
            
            // Single batched update - only 1 recomposition!
            android.util.Log.d("DeviceInfoViewModel", "✅ Batch 3 complete - updating state")
            _state.update {
                it.copy(
                    recentLogs = results[0],
                    fontInfo = results[1],
                    fileFormat = results[2],
                    securityInfo = results[3],
                    rootStatus = results[4],
                    usbDebugging = results[5],
                    aiInferenceSupport = results[6],
                    thermalZoneInfo = results[7],
                    spoofingStatus = results[8],
                    hiddenAppsStatus = results[9],
                    hackability = results[10],
                    faceUnlockTrust = results[11],
                    voiceCloneRisk = results[12],
                    adTracking = results[13],
                    isDeviceBeingMonitored = results[14],
                    isLoadingTertiary = false
                )
            }
            android.util.Log.d("DeviceInfoViewModel", "✅ Batch 3 state updated")
        }
    }
    
    /**
     * Update FPS-related info (called from main thread)
     * Only updates non-empty values to preserve existing data
     */
    fun updateFpsInfo(frameRate: String, frameDropData: String) {
        _state.update { currentState ->
            // Only update if values actually changed to prevent unnecessary recompositions
            val newFrameRate = if (frameRate.isNotEmpty() && frameRate != currentState.frameRate) frameRate else currentState.frameRate
            val newFrameDropData = if (frameDropData.isNotEmpty() && frameDropData != currentState.frameDropData) frameDropData else currentState.frameDropData
            
            // Only create new state if something changed
            if (newFrameRate != currentState.frameRate || newFrameDropData != currentState.frameDropData) {
                // android.util.Log.d("DeviceInfoViewModel", "✅ updateFpsInfo: frameRate='$newFrameRate' (was '${currentState.frameRate}'), frameDropData='${newFrameDropData.take(50)}...' (was '${currentState.frameDropData.take(50)}...')") // Disabled: Too verbose
                currentState.copy(
                    frameRate = newFrameRate,
                    frameDropData = newFrameDropData
                )
            } else {
                // android.util.Log.d("DeviceInfoViewModel", "⏭️ updateFpsInfo: no change, skipping update") // Disabled: Too verbose
                currentState // Return same instance to prevent recomposition
            }
        }
    }
    
    /**
     * Update GPU details (called from main thread)
     */
    fun updateGpuDetails(gpuDetails: String) {
        _state.update {
            it.copy(gpuDetails = gpuDetails)
        }
    }
}

