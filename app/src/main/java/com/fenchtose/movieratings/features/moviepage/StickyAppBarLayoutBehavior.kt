package com.fenchtose.movieratings.features.moviepage

import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import android.view.View

class StickyAppBarLayoutBehavior: AppBarLayout.Behavior() {
    override fun layoutDependsOn(parent: androidx.coordinatorlayout.widget.CoordinatorLayout, child: AppBarLayout, dependency: View): Boolean {
        return dependency is NestedScrollView
    }


}