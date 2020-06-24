package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils
import com.fenchtose.movieratings.util.emptyAsNull

class NetflixReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return findText(info, "video_details_title").emptyAsNull() ?: findAccessibilityDescription(info, "video_img")
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return findText(info, "video_details_basic_info_year")
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixNetflixYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_NETFLIX

}