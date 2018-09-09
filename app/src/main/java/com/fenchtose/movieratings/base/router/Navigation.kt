package com.fenchtose.movieratings.base.router

import com.fenchtose.movieratings.base.AppState
import com.fenchtose.movieratings.base.BaseFragment
import com.fenchtose.movieratings.base.RouterPath
import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.base.redux.Dispatch
import com.fenchtose.movieratings.base.redux.Next

data class Navigation(val router: Router, val path: RouterPath<out BaseFragment>): Action

fun navigator(state: AppState, action: Action, dispatch: Dispatch, next: Next<AppState>): Action {
    return when(action) {
        is Navigation -> {
            action.router.go(action.path)
            return action.path.initAction()
        }

        else -> next(state, action, dispatch)
    }
}