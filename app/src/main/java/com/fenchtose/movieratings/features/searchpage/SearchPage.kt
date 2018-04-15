package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.model.Movie

interface SearchPage {
    fun updateState(state: State)

    sealed class State {
        class Default: State()
        class Loading: State()
        class Error: State()

        sealed class Loaded(val movies: ArrayList<Movie>): State() {
            class Success(movies: ArrayList<Movie>): Loaded(movies)
            class PaginationSuccess(movies: ArrayList<Movie>): Loaded(movies)
            class Restored(movies: ArrayList<Movie>): Loaded(movies)
        }

        class LoadingMore: State()
        class PaginationError: State()

    }
}