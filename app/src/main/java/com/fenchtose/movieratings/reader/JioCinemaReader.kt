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
        return findText(info, "tvShowName")
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return findText(info, "tvMovieSubtitle")
            .filter {
                !FixTitleUtils.fixJioCinemaYear(it.toString()).isNullOrEmpty()
            }
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixJioCinemaYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_JIO_CINEMA

}