package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils

class HotstarReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        val nodes = ArrayList<CharSequence>()
        // it.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_HOTSTAR + ":id/metadata_title").filter { it.text != null }.map { it.text }
        if (event.className == "in.startv.hotstar.rocky.detailpage.HSDetailPageActivity" && event.text != null) {
            val text = event.text.toString().replace("[", "").replace("]", "")
            nodes.add(text)
        }
        return nodes
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_HOTSTAR + ":id/metadata_subtitle")
            .filter { it.text != null }
            .map {
                val text = it.text
                it.recycle()
                text
            }
            .filter {
                !FixTitleUtils.fixHotstarYear(it.toString()).isNullOrEmpty()
            }
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixHotstarYear(text) ?: ""
    }

}