package com.fenchtose.movieratings.base.redux

import android.util.Log
import com.fenchtose.movieratings.BuildConfig

fun<State> logger(state: State, action: Action, dispatch: Dispatch, next: Next<State>): Action {
    if (BuildConfig.DEBUG) {
        Log.d("store-middleware", "---> in $action")
        val retval = next(state, action, dispatch)
        Log.d("store-middleware", "<--- out $retval")
        return retval
    }

    return next(state, action, dispatch)
}