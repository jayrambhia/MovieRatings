package com.fenchtose.movieratings.features.debugging

import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath

class DebugOptionsFragment: BaseFragment() {
    override fun canGoBack() = true
    override fun getScreenTitle() = 0
    override fun screenName() =  GaScreens.DEBUGGING
}

class DebugOptionsPath: RouterPath<DebugOptionsFragment>() {
    override fun createFragmentInstance(): DebugOptionsFragment {
        return DebugOptionsFragment()
    }
}