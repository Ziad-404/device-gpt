package com.teamz.lab.debugger.utils

import android.os.Build

/**
 * Device Name Normalizer
 * 
 * Normalizes device names to handle variants (e.g., "SM-S918B" vs "SM-S918U" as same device).
 * Also creates a unique device identifier that cannot be changed by user (using Build.FINGERPRINT).
 */
object DeviceNameNormalizer {
    /**
     * Normalize device name to handle variants
     * Rules:
     * 1. Remove region codes (last letter/number)
     * 2. Remove carrier codes
     * 3. Standardize brand names
     * 4. Use fingerprint as fallback for unique identification
     */
    fun normalizeDeviceName(
        model: String = Build.MODEL,
        brand: String = Build.BRAND,
        manufacturer: String = Build.MANUFACTURER,
        fingerprint: String = Build.FINGERPRINT
    ): NormalizedDevice {
        // Extract base model (remove region variants)
        val baseModel = extractBaseModel(model, brand)
        
        // Standardize brand name
        val normalizedBrand = normalizeBrand(brand, manufacturer)
        
        // Create normalized identifier
        val normalizedId = createNormalizedId(normalizedBrand, baseModel)
        
        // Use fingerprint as unique identifier for same hardware (cannot be changed by user)
        val hardwareId = extractHardwareId(fingerprint)
        
        return NormalizedDevice(
            originalModel = model,
            normalizedModel = baseModel,
            normalizedBrand = normalizedBrand,
            normalizedId = normalizedId,
            hardwareId = hardwareId,
            displayName = "$normalizedBrand $baseModel"
        )
    }
    
    /**
     * Extract base model by removing region/carrier codes
     */
    private fun extractBaseModel(model: String, brand: String): String {
        // Remove Samsung region codes (SM-XXXXX -> SM-XXXX)
        if (model.startsWith("SM-")) {
            val parts = model.split("-")
            if (parts.size >= 2) {
                val modelCode = parts[1]
                // Remove last character (region code) if it's a letter
                if (modelCode.length > 4 && modelCode.last().isLetter()) {
                    val baseCode = modelCode.dropLast(1)
                    return "SM-$baseCode"
                }
            }
        }
        
        // Remove Xiaomi region codes (e.g., "2201116SG" -> "2201116")
        if (model.matches(Regex("^\\d+[A-Z]+$"))) {
            return model.replace(Regex("[A-Z]+$"), "")
        }
        
        // Remove underscore-based region codes (e.g., "POCO_X3_Pro" -> "POCO_X3")
        if (model.contains("_")) {
            val parts = model.split("_")
            // Keep all parts except the last if it's a single letter/number
            if (parts.size > 1 && parts.last().length <= 2) {
                return parts.dropLast(1).joinToString("_")
            }
        }
        
        // Remove carrier suffixes (e.g., "-ATT", "-TMO", "-VZW")
        return model.replace(Regex("-[A-Z]{1,3}$"), "")
            .replace(Regex("_[A-Z]{1,3}$"), "")
    }
    
    /**
     * Normalize brand name to standard format
     */
    private fun normalizeBrand(brand: String, manufacturer: String): String {
        val combined = "$brand $manufacturer".lowercase()
        
        return when {
            combined.contains("samsung") -> "Samsung"
            combined.contains("xiaomi") || combined.contains("redmi") -> "Xiaomi"
            combined.contains("oneplus") -> "OnePlus"
            combined.contains("oppo") -> "OPPO"
            combined.contains("vivo") -> "vivo"
            combined.contains("realme") -> "realme"
            combined.contains("google") || combined.contains("pixel") -> "Google"
            combined.contains("huawei") -> "Huawei"
            combined.contains("honor") -> "Honor"
            combined.contains("motorola") || combined.contains("moto") -> "Motorola"
            combined.contains("nokia") -> "Nokia"
            combined.contains("sony") -> "Sony"
            combined.contains("lg") -> "LG"
            combined.contains("asus") -> "ASUS"
            combined.contains("lenovo") -> "Lenovo"
            else -> brand.replaceFirstChar { it.uppercaseChar() }
        }
    }
    
    /**
     * Create normalized identifier for grouping
     */
    private fun createNormalizedId(brand: String, model: String): String {
        return "${brand}_${model}".lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace(Regex("[^a-z0-9_]"), "")
    }
    
    /**
     * Extract hardware identifier from fingerprint
     * This is unique per device and cannot be changed by user
     * Format: brand/model/device:version/...
     */
    private fun extractHardwareId(fingerprint: String): String {
        // Extract hardware identifier from fingerprint
        // Format: brand/model/device:version/...
        val parts = fingerprint.split("/")
        if (parts.size >= 3) {
            // Use brand/model as hardware identifier
            val brandModel = "${parts[0]}_${parts[1]}".lowercase()
            return brandModel.replace(Regex("[^a-z0-9_]"), "")
        }
        // Fallback: use hash of fingerprint
        return fingerprint.hashCode().toString()
    }
    
    /**
     * Get unique device identifier that cannot be changed by user
     * Uses Build.FINGERPRINT which is hardware-specific
     */
    fun getUniqueDeviceId(): String {
        return extractHardwareId(Build.FINGERPRINT)
    }
}

/**
 * Normalized device information
 */
data class NormalizedDevice(
    val originalModel: String,
    val normalizedModel: String,
    val normalizedBrand: String,
    val normalizedId: String, // For grouping in leaderboard
    val hardwareId: String, // For hardware-level grouping (unique, cannot be changed)
    val displayName: String // User-friendly name
)

