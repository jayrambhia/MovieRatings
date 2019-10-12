package com.fenchtose.movieratings.base

import androidx.annotation.StringRes

interface FragmentNavigation {
    fun canGoBack() : Boolean

    @StringRes
    fun getScreenTitle(): Int

    fun screenName(): String
}