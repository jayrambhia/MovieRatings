package com.fenchtose.movieratings.features.baselistpage

import com.fenchtose.movieratings.base.redux.Action
import com.fenchtose.movieratings.model.entity.Movie

data class BaseMovieListPageState(
    val movies: List<Movie> = listOf(),
    val progress: Progress = Progress.Default
)

sealed class Progress {
    object Default: Progress()
    object Loading: Progress()
    object Error: Progress()
    object Success: Progress()
}

sealed class BaseMovieListPageAction(val page: String): Action {
    class Loading(page: String): BaseMovieListPageAction(page)
    class Error(page: String): BaseMovieListPageAction(page)
    class Loaded(page: String, val movies: List<Movie>): BaseMovieListPageAction(page)
}