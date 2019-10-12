package com.fenchtose.movieratings.features.debugging

import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.analytics.ga.AppScreens

class DebugOptionsFragment: BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = 0
    override fun screenName() =  AppScreens.DEBUGGING
}

class DebugOptionsPath: RouterPath<DebugOptionsFragment>() {
    override fun createFragmentInstance(): DebugOptionsFragment {
        return DebugOptionsFragment()
    }
}