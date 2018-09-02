package com.fenchtose.movieratings.base

import android.view.View
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.NoAction
import com.fenchtose.movieratings.R
import com.fenchtose.movieratings.base.router.Router

abstract class RouterPath<T : BaseFragment> {

    var fragment: T? = null
    private var savedState: PresenterState? = null
    private var router: Router? = null

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

    fun saveState() {
        savedState = fragment?.saveState()
    }

    fun restoreState(): PresenterState? {
        val temp = savedState
        clearState()
        return temp
    }

    fun clearState() {
        savedState = null
    }

    fun getRouter(): Router? {
        return router
    }

    fun attachRouter(router: Router) {
        this.router = router
    }

    fun detach() {
        this.router = null
    }

    abstract fun createFragmentInstance(): T

    open fun showMenuIcons(): IntArray = intArrayOf()

    open fun showBackButton(): Boolean = true

    open fun getSharedTransitionElement(): Pair<View, String>? = null

    open fun category(): String = ""

    open fun toolbarElevation(): Int = R.dimen.toolbar_default_elevation

    open fun initAction(): Action = NoAction

    open fun clearAction(): Action = NoAction

}