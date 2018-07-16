package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
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
            // Drawing over apps is not supported for this device. So just check for accessibility
            if (!isDrawOverWindowSupported(context)) {
                return isAccessibilityEnabled(context)
            }

            return isAccessibilityEnabled(context)
                    && isDrawPermissionEnabled(context)
        }

        fun isAccessibilityEnabled(context: Context) : Boolean {

            val id: String = BuildConfig.APPLICATION_ID + "/." + NetflixReaderService::class.java.simpleName
            val fallback: String = BuildConfig.APPLICATION_ID + "/" + NetflixReaderService::class.java.canonicalName

            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val runningServices = manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)

            for (service in runningServices) {
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
            val manager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager?
            if (manager != null && manager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) {
                return true
            }

            return false
        }

        fun canDrawOverWindow(context: Context): Boolean {
            if (!isDrawOverWindowSupported(context)) {
                return false
            }

            // Check for xiaomi devices and other crash device?

            return isDrawPermissionEnabled(context)
        }

        fun isManufacturer(name: String): Boolean {
            return Build.MANUFACTURER.toLowerCase() == name
        }

        /**
         * Certain devices don't support drawing over apps (eg. TV) and certain devices crash (eg. xiaomi).
         * returns true if drawing over apps is enabled for this device
         */
        fun isDrawOverWindowSupported(context: Context): Boolean {
            return !(isTV(context) || isManufacturer("xiaomi"))
        }
    }
}