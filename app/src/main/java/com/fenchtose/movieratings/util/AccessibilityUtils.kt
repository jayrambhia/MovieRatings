package com.fenchtose.movieratings.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager

class AccessibilityUtils {
    companion object {

        val TAG = "AccessibilityUtils"

        fun isAccessibilityEnabled(context: Context, id: String) : Boolean {
            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val runningServices = manager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
            for (service in runningServices) {
                if (id.equals(service.id)) {
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