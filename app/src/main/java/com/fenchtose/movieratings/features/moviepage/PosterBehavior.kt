package com.fenchtose.movieratings.features.moviepage

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.view.View
import android.widget.ImageView

class PosterBehavior: CoordinatorLayout.Behavior<ImageView>() {

    private var h = 0
    private var w = 0
    private var diff = 0
    private var x = -1f

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ImageView, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ImageView, dependency: View): Boolean {

        if (w == 0) {
            w = child.measuredWidth
        }
        if (h == 0) {
            h = child.measuredHeight
        }
        if (diff == 0) {
            diff = child.height - dependency.height
        }
        if (x == -1f) {
            x = child.x
        }

        val scale = (dependency.height + diff + dependency.y)/(dependency.height + diff)

        child.scaleY = scale
        child.scaleX = scale

        child.y = (scale-1) * h/2
        child.x = x + (scale - 1) * w/2

        return true
    }
}