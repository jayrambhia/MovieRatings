package com.fenchtose.movieratings.features.moviepage

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v4.widget.NestedScrollView
import android.view.View

class StickyAppBarLayoutBehavior: AppBarLayout.Behavior() {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: AppBarLayout, dependency: View): Boolean {
        return dependency is NestedScrollView
    }


}