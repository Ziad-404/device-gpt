package com.teamz.lab.debugger.utils

import android.content.Context
import androidx.annotation.StringRes

/**
 * Extension function to easily get string resources in Compose
 * Usage: context.getString(R.string.app_name) becomes context.string(R.string.app_name)
 */
fun Context.string(@StringRes resId: Int): String {
    return getString(resId)
}

/**
 * Extension function to get string resources with format arguments
 * Usage: context.string(R.string.welcome_message, userName)
 */
fun Context.string(@StringRes resId: Int, vararg formatArgs: Any): String {
    return getString(resId, *formatArgs)
}

