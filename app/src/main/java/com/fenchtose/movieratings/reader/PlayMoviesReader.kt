package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants
import com.fenchtose.movieratings.util.FixTitleUtils

class PlayMoviesReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        val nodes = ArrayList<CharSequence>()
        if (event.className == "com.google.android.apps.play.movies.mobile.usecase.details.DetailsActivity" && event.text != null) {
            val text = event.text.toString().replace("[", "").replace("]", "")
            nodes.add(text)
        }
        return nodes
    }

    override fun readYear(info: AccessibilityNodeInfo): List<CharSequence> {
        val nodes = ArrayList<CharSequence>()
        info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_PLAY_MOVIES_TV + ":id/play_header_listview")
            .takeIf { it.size > 0 }
            ?.firstOrNull()
            ?.run {
                (0 until childCount).map { i ->
                    getChild(i)
                }.filter {
                    it != null && it.text != null && it.className.contains("TextView") && FixTitleUtils.matchesPlayMoviesYear(
                        it.text.toString()
                    )
                }.map {
                    val text = it.text
                    it.recycle()
                    text
                }.toCollection(nodes)
            }

        return nodes
    }

    override fun fixYear(text: String): String {
        return FixTitleUtils.fixPlayMoviesYear(text) ?: ""
    }

    override fun getAppId() = Constants.PACKAGE_PLAY_MOVIES_TV

}