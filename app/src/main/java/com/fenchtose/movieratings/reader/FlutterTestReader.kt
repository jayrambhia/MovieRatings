package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.BuildConfig

class FlutterTestReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return findText(info, "flutter_test_title")
    }

    override fun getAppId() = BuildConfig.APPLICATION_ID

}