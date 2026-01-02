package com.teamz.lab.debugger.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Global error handling utility
 * Handles both fatal and non-fatal exceptions
 */
object ErrorHandler {
    private const val TAG = "ErrorHandler"
    
    /**
     * Handle a non-fatal exception (doesn't crash the app)
     * Use this for recoverable errors
     */
    fun handleError(exception: Exception, context: String = "") {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // Add custom context if provided
            if (context.isNotEmpty()) {
                crashlytics.setCustomKey("error_context", context)
            }
            
            // Record as non-fatal exception
            crashlytics.recordException(exception)
            
            // Log to console for debugging
            Log.e(TAG, "Non-fatal error${if (context.isNotEmpty()) " in $context" else ""}: ${exception.message}", exception)
            exception.printStackTrace()
        } catch (e: Exception) {
            // Fallback if Crashlytics fails
            Log.e(TAG, "Failed to log error to Crashlytics: ${e.message}", e)
            exception.printStackTrace()
        }
    }
    
    /**
     * Handle a fatal exception (crashes the app)
     * Use this for critical errors that should crash the app
     */
    fun handleFatalError(exception: Exception, context: String = "") {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // Add custom context if provided
            if (context.isNotEmpty()) {
                crashlytics.setCustomKey("fatal_error_context", context)
                crashlytics.setCustomKey("is_fatal", true)
            }
            
            // Record as fatal exception
            crashlytics.recordException(exception)
            
            // Log to console
            Log.e(TAG, "FATAL error${if (context.isNotEmpty()) " in $context" else ""}: ${exception.message}", exception)
            exception.printStackTrace()
        } catch (e: Exception) {
            // Fallback if Crashlytics fails
            Log.e(TAG, "Failed to log fatal error to Crashlytics: ${e.message}", e)
            exception.printStackTrace()
        }
        
        // Re-throw to crash the app (for fatal errors)
        throw exception
    }
    
    /**
     * Handle a throwable (for uncaught exceptions)
     */
    fun handleThrowable(throwable: Throwable, context: String = "") {
        when (throwable) {
            is Exception -> handleError(throwable, context)
            is Error -> {
                // Errors are typically fatal
                try {
                    val crashlytics = FirebaseCrashlytics.getInstance()
                    if (context.isNotEmpty()) {
                        crashlytics.setCustomKey("error_context", context)
                    }
                    crashlytics.recordException(throwable)
                    Log.e(TAG, "Fatal error${if (context.isNotEmpty()) " in $context" else ""}: ${throwable.message}", throwable)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to log error: ${e.message}", e)
                }
                throwable.printStackTrace()
            }
            else -> {
                Log.e(TAG, "Unknown throwable${if (context.isNotEmpty()) " in $context" else ""}: ${throwable.message}", throwable)
                throwable.printStackTrace()
            }
        }
    }
    
    /**
     * Log a custom message to Crashlytics
     */
    fun logMessage(message: String, level: Int = Log.ERROR) {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.log(message)
            Log.println(level, TAG, message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log message to Crashlytics: ${e.message}", e)
        }
    }
    
    /**
     * Set custom key-value pair for debugging
     */
    fun setCustomKey(key: String, value: String) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: ${e.message}", e)
        }
    }
    
    /**
     * Set custom key-value pair for debugging (Int)
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: ${e.message}", e)
        }
    }
    
    /**
     * Set custom key-value pair for debugging (Boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: ${e.message}", e)
        }
    }
}

/**
 * Convenience function for backward compatibility
 */
fun handleError(exception: Exception, context: String = "") {
    ErrorHandler.handleError(exception, context)
}