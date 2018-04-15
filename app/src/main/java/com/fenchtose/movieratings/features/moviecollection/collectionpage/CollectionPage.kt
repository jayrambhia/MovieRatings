package com.fenchtose.movieratings.features.moviecollection.collectionpage

import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.model.Movie

interface CollectionPage: BaseMovieListPage {

//    fun onRemoved(movie: Movie, position: Int)
//    fun showAdded(movie: Movie, position: Int)
    fun updateState(state: OpState)

    sealed class OpState(val movie: Movie) {
        class Removed(movie: Movie, val position: Int): OpState(movie)
        class Added(movie: Movie, val position: Int): OpState(movie)
        class RemoveError(movie: Movie): OpState(movie)
        class AddError(movie: Movie): OpState(movie)
    }
}