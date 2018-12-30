package com.fenchtose.movieratings.util

import android.content.Context
import android.os.Build
import android.os.PowerManager

fun checkBatteryOptimized(context: Context) : Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val manager =
            context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        return !manager.isIgnoringBatteryOptimizations(context.packageName)
    }
    return false
}