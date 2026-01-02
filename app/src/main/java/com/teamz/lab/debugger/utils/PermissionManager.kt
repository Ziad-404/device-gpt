package com.teamz.lab.debugger.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission

/**
 * Centralized Permission Manager
 * Provides a single source of truth for all permission handling across the app
 * Eliminates code duplication and ensures consistent permission behavior
 */
object PermissionManager {
    
    // Permission constants
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    const val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    const val ACCESS_FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    const val ACCESS_COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
    const val BLUETOOTH_PERMISSION = Manifest.permission.BLUETOOTH
    const val BLUETOOTH_CONNECT_PERMISSION = Manifest.permission.BLUETOOTH_CONNECT
    const val READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE
    const val POST_NOTIFICATIONS_PERMISSION = Manifest.permission.POST_NOTIFICATIONS
    const val PACKAGE_USAGE_STATS_PERMISSION = Manifest.permission.PACKAGE_USAGE_STATS
    
    /**
     * Check if a specific permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if camera permission is granted
     */
    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, CAMERA_PERMISSION)
    }
    
    /**
     * Check if location permission is granted (either fine or coarse)
     */
    fun hasLocationPermission(context: Context): Boolean {
        return hasPermission(context, ACCESS_FINE_LOCATION_PERMISSION) ||
                hasPermission(context, ACCESS_COARSE_LOCATION_PERMISSION)
    }
    
    /**
     * Check if Bluetooth permissions are granted
     * Handles Android 12+ (API 31+) which requires BLUETOOTH_CONNECT
     */
    fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(context, BLUETOOTH_CONNECT_PERMISSION) &&
            hasPermission(context, BLUETOOTH_PERMISSION)
        } else {
            hasPermission(context, BLUETOOTH_PERMISSION)
        }
    }
    
    /**
     * Check if audio recording permission is granted
     */
    fun hasAudioPermission(context: Context): Boolean {
        return hasPermission(context, RECORD_AUDIO_PERMISSION)
    }
    
    /**
     * Check if phone state permission is granted
     */
    fun hasPhoneStatePermission(context: Context): Boolean {
        return hasPermission(context, READ_PHONE_STATE_PERMISSION)
    }
    
    /**
     * Check if notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(context, POST_NOTIFICATIONS_PERMISSION)
        } else {
            true // Permission not required on older Android versions
        }
    }
    
    /**
     * Check if usage stats permission is granted
     * Note: This requires special handling via Settings, not runtime permission
     */
    fun hasUsageStatsPermission(context: Context): Boolean {
        // Reuse existing utility function from network_utils
        return com.teamz.lab.debugger.utils.hasUsageStatsPermission(context)
    }
    
    /**
     * Check if WRITE_SETTINGS permission is granted
     * Note: This requires special handling via Settings, not runtime permission
     */
    fun hasWriteSettingsPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.System.canWrite(context)
        } else {
            true // Permission not required on older Android versions
        }
    }
    
    /**
     * Check if we should show permission rationale
     * Returns true if user has previously denied permission
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    /**
     * Get all required permissions for a component
     */
    fun getRequiredPermissions(component: String): List<String> {
        return when (component.lowercase()) {
            "camera" -> listOf(CAMERA_PERMISSION)
            "audio" -> listOf(RECORD_AUDIO_PERMISSION)
            "gps", "location" -> listOf(
                ACCESS_FINE_LOCATION_PERMISSION,
                ACCESS_COARSE_LOCATION_PERMISSION
            )
            "bluetooth" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(BLUETOOTH_CONNECT_PERMISSION, BLUETOOTH_PERMISSION)
                } else {
                    listOf(BLUETOOTH_PERMISSION)
                }
            }
            else -> emptyList()
        }
    }
    
    /**
     * Filter permissions to only those that are not already granted
     */
    fun filterUngrantedPermissions(context: Context, permissions: List<String>): List<String> {
        return permissions.filter { !hasPermission(context, it) }
    }
    
    /**
     * Composable function to remember a single permission launcher
     * Use this for requesting a single permission
     */
    @Composable
    fun rememberPermissionLauncher(
        onResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return rememberLauncherForActivityResult(
            contract = RequestPermission()
        ) { isGranted ->
            onResult(isGranted)
        }
    }
    
    /**
     * Composable function to remember a multiple permissions launcher
     * Use this for requesting multiple permissions at once
     */
    @Composable
    fun rememberMultiplePermissionsLauncher(
        onResult: (Map<String, Boolean>) -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return rememberLauncherForActivityResult(
            contract = RequestMultiplePermissions()
        ) { permissions ->
            onResult(permissions)
        }
    }
    
    /**
     * Request a single permission
     * Returns true if permission is already granted, false otherwise
     * Launches permission request if not granted
     */
    fun requestPermission(
        context: Context,
        activity: Activity?,
        permission: String,
        launcher: ActivityResultLauncher<String>
    ): Boolean {
        // Check if already granted
        if (hasPermission(context, permission)) {
            return true
        }
        
        // Request permission
        if (activity != null) {
            launcher.launch(permission)
        }
        
        return false
    }
    
    /**
     * Request multiple permissions
     * Returns true if all permissions are already granted, false otherwise
     * Launches permission request if any are not granted
     */
    fun requestPermissions(
        context: Context,
        permissions: List<String>,
        launcher: ActivityResultLauncher<Array<String>>
    ): Boolean {
        // Filter to only ungranted permissions
        val ungrantedPermissions = filterUngrantedPermissions(context, permissions)
        
        // If all granted, return true
        if (ungrantedPermissions.isEmpty()) {
            return true
        }
        
        // Request ungranted permissions
        launcher.launch(ungrantedPermissions.toTypedArray())
        
        return false
    }
}

