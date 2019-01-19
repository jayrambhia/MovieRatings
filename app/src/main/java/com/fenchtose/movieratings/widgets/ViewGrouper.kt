package com.fenchtose.movieratings.widgets

import android.view.View
import com.fenchtose.movieratings.util.show

class ViewGrouper(private val views: List<View>) {
    constructor(vararg views: View) : this(views.toList())
    fun show(status: Boolean) {
        views.forEach { it.show(status) }
    }
}