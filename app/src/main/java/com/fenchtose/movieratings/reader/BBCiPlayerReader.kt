package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants

class BBCiPlayerReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_BBC_IPLAYER + ":id/programme_details_title")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
    }
}