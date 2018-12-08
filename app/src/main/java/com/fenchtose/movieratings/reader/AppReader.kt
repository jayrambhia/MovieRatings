package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.FixTitleUtils

interface AppReader {
    fun readTitles(event: AccessibilityEvent, info: AccessibilityNodeInfo): List<CharSequence>
    fun readYear(info: AccessibilityNodeInfo): List<CharSequence> = listOf()
    fun fixTitle(text: String): String {
        return FixTitleUtils.clean(text)
    }

    fun fixYear(text: String): String = ""
}