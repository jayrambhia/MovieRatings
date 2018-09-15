package com.fenchtose.movieratings.base.router

import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Dispatch
import java.util.*

class RouterRoot(baseRoute: RouterPath<out BaseFragment>) {
    private val history: Stack<RouterPath<out BaseFragment>> = Stack()

    init {
        history.push(baseRoute)
    }

    fun build(dispatch: Dispatch?, path: RouterPath<out BaseFragment>, recreateRoot: Boolean = false) {
        if (recreateRoot) {
            while (history.isNotEmpty()) {
                clearPath(dispatch, history.pop())
            }
        }

        history.push(path)
    }

    fun top(): RouterPath<out BaseFragment> {
        return history.peek()
    }

    fun pop(): RouterPath<out BaseFragment>? {
        if (history.isNotEmpty()) {
            return history.pop()
        }

        return null
    }

    fun clearTillBase(dispatch: Dispatch?) {
        while (history.size != 1) {
           clearPath(dispatch, history.pop())
        }
    }

    private fun clearPath(dispatch: Dispatch?, path: RouterPath<out BaseFragment>) {
        path.detach()
        dispatch?.invoke(path.clearAction())
    }

    fun size(): Int = history.size
}