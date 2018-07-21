package com.fenchtose.movieratings.base.redux

typealias Reducer<State> = (State, Action) -> State

fun<State, Child> reduceChildState(
        state: State,
        child: Child,
        action: Action,
        reducer: Reducer<Child>,
        onReduced: (State, Child) -> State): State {

    val reduced = reducer.invoke(child, action)
    if (reduced === child) {
        return state
    }

    return onReduced(state, reduced)
}