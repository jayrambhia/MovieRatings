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
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_title")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_NETFLIX + ":id/video_details_basic_info_year")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixNetflixYear(text) ?: ""
    }

}