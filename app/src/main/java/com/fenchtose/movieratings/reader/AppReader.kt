package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

interface AppReader {
    fun readTitles(event: AccessibilityEvent, info: AccessibilityNodeInfo): List<CharSequence>
    fun readYear(info: AccessibilityNodeInfo): List<CharSequence> = listOf()

    fun fixYear(text: String): String = ""
    fun getAppId(): String

    fun findText(
        info: AccessibilityNodeInfo,
        viewId: String
    ): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId("${getAppId()}:id/$viewId")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
    }

    fun findAccessibilityDescription(info: AccessibilityNodeInfo, viewId: String): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId("${getAppId()}:id/$viewId")
            .filter { it.contentDescription != null }
            .map {
                val text = it.contentDescription
                it.recycle()
                text
            }
    }
}