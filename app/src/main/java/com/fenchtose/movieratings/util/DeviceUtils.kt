package com.fenchtose.movieratings.util

import android.content.Context
import android.os.Build
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.preferences.SettingsPreferences
import com.fenchtose.movieratings.model.preferences.UserPreferences

fun getDeviceInfo(): String {
    return "android version: ${Build.VERSION.SDK_INT}\n" +
            "device: ${Build.DEVICE}\n" +
            "model: ${Build.MODEL}\n" +
            "product: ${Build.PRODUCT}\n" +
            "manufacturer: ${Build.MANUFACTURER}"
}

fun getAppInfo(context: Context): String {
    val preferences = SettingsPreferences(context)
    return "app version: ${BuildConfig.VERSION_NAME}\n" +
            "flutter api: ${preferences.isAppEnabled(UserPreferences.USE_FLUTTER_API)}"
}