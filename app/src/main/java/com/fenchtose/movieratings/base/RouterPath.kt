package com.fenchtose.movieratings.base

import android.view.View

abstract class RouterPath<T : BaseFragment> {

    var fragment: T? = null

    private fun createFragment() : T {
        fragment = createFragmentInstance()
        (fragment as T).path = this
        return fragment as T
    }

    fun createOrGetFragment(): T {
        fragment?.let {
            return fragment as T
        }

        return createFragment()
    }

    abstract fun createFragmentInstance(): T

    open fun showMenuIcons(): IntArray = intArrayOf()

    open fun showBackButton(): Boolean = true

    open fun getSharedTransitionElement(): Pair<View, String>? = null

}