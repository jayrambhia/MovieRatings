package com.fenchtose.movieratings.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class PackageUtils {

    companion object {
        val NETFLIX = "com.netflix.mediaservice"

        fun hasInstalled(context: Context, packageName: String): Boolean {
            val packages = context.packageManager.getInstalledApplications(0)
            packages.forEach {
                if (it.packageName == packageName) {
                    return true
                }
            }
            return false
        }

        fun isIntentCallabale(context: Context, intent: Intent): Boolean {
            val activities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            return activities.size > 0
        }
    }

}