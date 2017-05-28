package com.fenchtose.movieratings.base

interface FragmentNavigation {
    fun canGoBack() : Boolean
    fun getScreenTitle(): String
}