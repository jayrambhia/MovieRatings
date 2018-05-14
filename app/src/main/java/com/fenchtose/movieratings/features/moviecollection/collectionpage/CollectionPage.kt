package com.fenchtose.movieratings.features.moviecollection.collectionpage

import android.net.Uri
import com.fenchtose.movieratings.features.baselistpage.BaseMovieListPage
import com.fenchtose.movieratings.model.Movie

interface CollectionPage: BaseMovieListPage {

    fun updateState(state: OpState)
    fun updateState(state: ShareState)

    sealed class OpState(val movie: Movie) {
        class Removed(movie: Movie, val position: Int): OpState(movie)
        class Added(movie: Movie, val position: Int): OpState(movie)
        class RemoveError(movie: Movie): OpState(movie)
        class AddError(movie: Movie): OpState(movie)
    }

    sealed class ShareState {
        class Started: ShareState()
        class Error: ShareState()
        class Success(val uri: Uri): ShareState()
    }
}