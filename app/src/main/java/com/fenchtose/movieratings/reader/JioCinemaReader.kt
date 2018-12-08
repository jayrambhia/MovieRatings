package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils

class JioCinemaReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_JIO_CINEMA + ":id/tvShowName")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_JIO_CINEMA + ":id/tvMovieSubtitle")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
            .filter {
                !FixTitleUtils.fixJioCinemaYear(it.toString()).isNullOrEmpty()
            }
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixJioCinemaYear(text) ?: ""
    }

}