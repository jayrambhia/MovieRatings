package com.fenchtose.movieratings.reader

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.fenchtose.movieratings.util.Constants

class YoutubeReader : AppReader {
    override fun readTitles(
        event: AccessibilityEvent,
        info: AccessibilityNodeInfo
    ): List<CharSequence> {
        val nodes = ArrayList<CharSequence>()
        info.findAccessibilityNodeInfosByViewId(Constants.PACKAGE_YOUTUBE + ":id/watch_list").filter { it.childCount > 0 }
            .flatMap {
                findChildrenWithId(it, Constants.PACKAGE_YOUTUBE + ":id/title")
                    .filter { it.text != null && it.parent?.viewIdResourceName == null}
                    .map {
                        val text = it.text
                        it.recycle()
                        text
                    }
            }.toCollection(nodes)
        return nodes
    }

    private fun findChildrenWithId(node: AccessibilityNodeInfo, id: String): ArrayList<AccessibilityNodeInfo> {
        val children = ArrayList<AccessibilityNodeInfo>()
        recursiveFindChildrenWithId(children, node, id)
        return children
    }

    private fun recursiveFindChildrenWithId(result: ArrayList<AccessibilityNodeInfo>, node: AccessibilityNodeInfo, id: String) {
        var added = false
        if (node.viewIdResourceName == id) {
            result.add(node)
            added = true
        }

        if (node.childCount > 0) {
            (0 until node.childCount)
                .forEach {
                        index ->
                    val child = node.getChild(index)
                    recursiveFindChildrenWithId(result, child, id)
                }
        }

        if (!added) {
            node.recycle()
        }
    }

}