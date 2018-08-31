package com.fenchtose.movieratings.base

import android.support.annotation.StringRes

interface FragmentNavigation {
    fun canGoBack() : Boolean

    @StringRes
    fun getScreenTitle(): Int

    fun screenName(): String
}