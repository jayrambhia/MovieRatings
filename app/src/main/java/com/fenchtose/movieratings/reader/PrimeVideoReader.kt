package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils
import com.fenchtose.movieratings.util.emptyAsNull
import java.lang.IllegalStateException

class PrimeVideoReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        return findText(info, "header_title_text").emptyAsNull() ?: findText(info, "TitleText")
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        return findText(info, "header_date_released")
            .filter { !FixTitleUtils.fixPrimeVideoYear(it.toString()).isNullOrEmpty() }
            .emptyAsNull() ?: readYearLegacy(info)
    }

    private fun readYearLegacy(info: AccessibilityNodeInfo) : List<CharSequence> {
        return info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PRIMEVIDEO + ":id/ItemMetadataView")
            // get children of that node
            .flatMap {
                val children = ArrayList<CharSequence>()
                (0 until it.childCount).map { i ->
                    var text: CharSequence
                    try {
                        val child = it.getChild(i)
                        text = child.text
                        child.recycle()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        text = ""
                    }
                    text
                }.filter {
                    it != null && it.isNotBlank()
                }
                    .toCollection(children)
                children
            }
            // filter node which has text containing 4 digits
            .filter {
                !FixTitleUtils.fixPrimeVideoYear(it.toString()).isNullOrEmpty()
            }
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixPrimeVideoYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_PRIMEVIDEO

}