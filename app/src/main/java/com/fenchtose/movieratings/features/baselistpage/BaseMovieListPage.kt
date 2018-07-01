package com.fenchtose.movieratings.features.baselistpage

import com.fenchtose.movieratings.model.entity.Movie

interface BaseMovieListPage {

    fun updateState(state: State)

    sealed class State {
        class Loading: State()
        class Success(val movies: List<Movie>): State()
        class Empty: State()
        class Error: State()
    }
}