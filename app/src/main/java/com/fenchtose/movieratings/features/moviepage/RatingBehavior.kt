package com.fenchtose.movieratings.features.moviepage

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.view.View
import com.fenchtose.movieratings.R

class RatingBehavior(context: Context) : CoordinatorLayout.Behavior<View>() {

    private var x = -1f
    private var y = -1f

    private val pinHeight = context.resources.getDimensionPixelOffset(R.dimen.movie_app_bar_pin_height)
    private val xMovement = context.resources.getDimensionPixelOffset(R.dimen.movie_rating_x_movement)

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        if (y == -1f) {
            y = child.y
        }

        if (x == -1f) {
            x = child.x
        }

        val diff = pinHeight + dependency.y
        val scale = 1 - diff/pinHeight

        child.y = y + (dependency.y)/2
        child.x = x - xMovement * scale
        return true
    }
}