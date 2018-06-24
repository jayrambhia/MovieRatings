package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection

interface SearchPage {
    fun updateState(state: State)
    fun updateState(state: CollectionState)

    sealed class State {
        object Default: State()
        object Loading: State()
        object Error: State()
        object NoResult: State()

        sealed class Loaded(val movies: ArrayList<Movie>): State() {
            class Success(movies: ArrayList<Movie>): Loaded(movies)
            class PaginationSuccess(movies: ArrayList<Movie>): Loaded(movies)
            class Restored(movies: ArrayList<Movie>): Loaded(movies)
        }

        object LoadingMore: State()
        object PaginationError: State()
    }

    sealed class CollectionState(val collection: MovieCollection) {
        class Exists(collection: MovieCollection): CollectionState(collection)
        class Added(collection: MovieCollection): CollectionState(collection)
        class Error(collection: MovieCollection): CollectionState(collection)
    }
}