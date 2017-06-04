package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
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

        fun isTV(context: Context): Boolean {
            val manager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
            if (manager != null && manager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
                return true
            }

            return false
        }

        fun canDrawOverWindow(context: Context): Boolean {
            if (isTV(context)) {
                return false
            }

            // Check for xiaomi devices and other crash device?

            return isDrawPermissionEnabled(context)
        }
    }
}