package com.fenchtose.movieratings.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class PackageUtils {

    companion object {
        const val NETFLIX = "com.netflix.mediaclient"

        fun hasInstalled(context: Context, packageName: String): Boolean {
            val packages = context.packageManager.getInstalledApplications(0)
            packages.forEach {
                if (it.packageName == packageName) {
                    return true
                }
            }
            return false
        }

        fun isPackageInstalled(context: Context, packageName: String): Boolean {
            return try {
                context.packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        fun isIntentCallabale(context: Context, intent: Intent): Boolean {
            val activities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            return activities.size > 0
        }
    }

}