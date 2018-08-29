package com.fenchtose.movieratings.util

import android.support.design.widget.TabLayout
import android.view.View
import android.view.ViewGroup

fun TabLayout.stylizeIndicator() {
    for(i in 0 until tabCount) {
        val tab = getTabAt(i)
        tab?.customView?.let {
            val target = it.parent as View
            (target.layoutParams as ViewGroup.MarginLayoutParams).apply {
                rightMargin = 50
                leftMargin = 50
                target.layoutParams = this
                target.requestLayout()
            }
        }
    }
}