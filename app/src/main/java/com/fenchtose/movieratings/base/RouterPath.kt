package com.fenchtose.movieratings.base

abstract class RouterPath<T : BaseFragment> {

    var fragment: T? = null

    private fun createFragment() : T {
        fragment = createFragmentInstance()
        return fragment as T
    }

    fun createOrGetFragment(): T {
        fragment?.let {
            return fragment as T
        }

        return createFragment()
    }

    abstract fun createFragmentInstance(): T

    open fun showMenuIcons(): IntArray {
        return intArrayOf()
    }
}