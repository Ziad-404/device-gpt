package com.teamz.lab.debugger.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Locale Helper - Demonstrates how Android automatically detects device language
 * 
 * IMPORTANT: Android automatically detects the device locale (language + country)
 * when you use context.getString() or context.string(). No code needed!
 * 
 * This helper is just for demonstration/debugging purposes.
 */
object LocaleHelper {
    
    /**
     * Get the current device locale
     * Android automatically uses this when loading string resources
     */
    fun getCurrentLocale(context: Context): Locale {
        return context.resources.configuration.locales[0]
    }
    
    /**
     * Get the current device language code (e.g., "en", "bn", "es")
     */
    fun getCurrentLanguage(context: Context): String {
        return getCurrentLocale(context).language
    }
    
    /**
     * Get the current device country code (e.g., "US", "BD", "IN")
     */
    fun getCurrentCountry(context: Context): String {
        return getCurrentLocale(context).country
    }
    
    /**
     * Check if device is set to Bengali
     */
    fun isBengali(context: Context): Boolean {
        return getCurrentLanguage(context) == "bn"
    }
    
    /**
     * Get full locale info for debugging
     */
    fun getLocaleInfo(context: Context): String {
        val locale = getCurrentLocale(context)
        return """
            Language: ${locale.language}
            Country: ${locale.country}
            Display Name: ${locale.displayName}
            Full Locale: ${locale.toString()}
            
            Android will automatically use: values-${locale.language}/strings.xml
            If not found, falls back to: values/strings.xml (English)
        """.trimIndent()
    }
}

/**
 * How Android Language Detection Works:
 * 
 * 1. User sets device language in: Settings → System → Languages
 * 2. Android stores this as the device locale (e.g., "bn_BD" for Bengali-Bangladesh)
 * 3. When your app calls context.getString(R.string.xxx):
 *    - Android checks: values-bn/strings.xml (if device is Bengali)
 *    - If found: Uses Bengali strings
 *    - If not found: Falls back to values/strings.xml (English)
 * 
 * 4. NO CODE NEEDED - It's automatic!
 * 
 * Example:
 * - Device language: Bengali (বাংলা)
 * - Android looks for: values-bn/strings.xml
 * - Your code: context.string(R.string.power_consumption)
 * - Result: Shows "পাওয়ার খরচ" (Bengali) automatically
 * 
 * To test:
 * 1. Change device language to Bengali in Settings
 * 2. Restart app
 * 3. All strings will show in Bengali (if translated)
 */

