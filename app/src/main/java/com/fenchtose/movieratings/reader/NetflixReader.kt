package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils

class NetflixReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return findText(info, "video_details_title")
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return findText(info, "video_details_basic_info_year")
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixNetflixYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_NETFLIX

}