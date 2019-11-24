package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils

class DisneyReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return findText(info, "title")
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return findText(info, "metaData")
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixDisneyYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_DISNEY

}