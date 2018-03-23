package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.model.Movie

interface CollectionPage: BaseMovieListPage {

    fun onRemoved(movie: Movie, position: Int)
    fun showAdded(movie: Movie, position: Int)
    fun updateState(state: OpState)

    data class OpState(val op: Op, val movie: Movie, val position: Int = -1)

    enum class Op {
        MOVIE_REMOVED,
        MOVIE_REMOVE_ERROR,
        MOVIE_ADDED,
        MOVIE_ADD_ERROR
    }
}