package com.fenchtose.movieratings.base.redux

import android.os.Looper
import android.util.Log
import com.fenchtose.movieratings.BuildConfig

interface Action

object NoAction: Action

typealias Dispatch = (Action) -> Unit

typealias Middleware<State> = (State, Action, Dispatch, Next<State>) -> Action

typealias Next<State> = (State, Action, Dispatch) -> Action

typealias Subscription<State> = (State, Dispatch) -> Unit

typealias Unsubscribe = () -> Unit

interface Store<State> {
    fun subscribe(subscription: Subscription<State>): Unsubscribe
}

abstract class SimpleStore<State>(initialState: State,
                                  private val reducers: List<Reducer<State>>,
                                  private val middlewares: List<Middleware<State>>): Store<State> {

    private var viewMiddlewares = listOf<Middleware<State>>()
    private val subscriptions = arrayListOf<Subscription<State>>()
    private var _state: State = initialState

    private fun _dispatch(action: Action) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException("dispatch() should be called only on main thread!")
        }

        val new = reduce(_state, dispatchToMiddleWare(action))
        if (_state == new) {
            // no updates
            if (BuildConfig.DEBUG) {
                Log.d("Simple-Store", "no state change for action: $action")
            }
            return
        }

        if (BuildConfig.DEBUG) {
            Log.d("Simple-Store", "reduce action: $action\nprev state: ${_state?.hashCode()}\nnew state: ${new?.hashCode()}")
        }
        _state = new
        subscriptions.forEach { it(_state, ::dispatch) }
    }

    private fun dispatchToMiddleWare(action: Action): Action {
        val mutated = next(0)(_state, action, ::dispatch)
        return nextViewMiddleware(0)(_state, mutated, ::dispatch)
    }

    private fun next(index: Int): Next<State> {
        if (index == middlewares.size) {
            return { _, action, _ -> action }
        }
        return { state, action, dispatch -> middlewares[index].invoke(state, action, dispatch, next(index + 1))}
    }

    private fun nextViewMiddleware(index: Int): Next<State> {
        if (index == viewMiddlewares.size) {
            return { _, action, _ -> action }
        }
        return { state, action, dispatch -> viewMiddlewares[index].invoke(state, action, dispatch, nextViewMiddleware(index + 1))}
    }

    private fun reduce(current: State, action: Action): State {
        var new = current
        for (reducer in reducers) {
            new = reduce(reducer, new, action)
        }

        return new
    }

    private fun reduce(reducer: Reducer<State>, state: State, action: Action): State {
        val newState = reducer.invoke(state, action)
        return newState
    }

    override fun subscribe(subscription: Subscription<State>): Unsubscribe  {
        subscriptions.add(subscription)
        subscription(_state, ::dispatch)
        return { subscriptions.remove(subscription) }
    }

    fun addViewMiddleware(middleware: Middleware<State>): Unsubscribe {
        this.viewMiddlewares += middleware
        return { this.viewMiddlewares -= middleware }
    }

    fun dispatch(action: Action) {
        _dispatch(action)
    }

}