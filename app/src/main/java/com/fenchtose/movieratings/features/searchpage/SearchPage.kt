package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection

interface SearchPage {
    fun updateState(state: State)
    fun updateState(state: CollectionState)

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

    sealed class CollectionState(val collection: MovieCollection) {
        class Exists(collection: MovieCollection): CollectionState(collection)
        class Added(collection: MovieCollection): CollectionState(collection)
        class Error(collection: MovieCollection): CollectionState(collection)
    }
}