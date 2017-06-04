package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.NetflixReaderService

class AccessibilityUtils {
    companion object {

        val TAG = "AccessibilityUtils"

        fun hasAllPermissions(context: Context): Boolean {
           return isAccessibilityEnabled(context)
                    && isDrawPermissionEnabled(context)
        }

        fun isAccessibilityEnabled(context: Context) : Boolean {

            val id: String = BuildConfig.APPLICATION_ID + "/." + NetflixReaderService::class.java.simpleName
            val fallback: String = BuildConfig.APPLICATION_ID + "/" + NetflixReaderService::class.java.canonicalName

            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val runningServices = manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)

            Log.d(TAG, "found service: " + runningServices)

            for (service in runningServices) {
                Log.d(TAG, "service:" + service.id)
                if (id == service.id || fallback == service.id) {
                    return true
                }
            }

            return false
        }

        @SuppressLint("NewApi")
        fun isDrawPermissionEnabled(context: Context): Boolean {
            if (VersionUtils.isMOrAbove()) {
                return Settings.canDrawOverlays(context)
            }

            return true
        }
    }
}