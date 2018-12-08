package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.BuildConfig

class FlutterTestReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(BuildConfig.APPLICATION_ID + ":id/flutter_test_title")
            .filter { it.text != null }.map { it.text }
    }

}