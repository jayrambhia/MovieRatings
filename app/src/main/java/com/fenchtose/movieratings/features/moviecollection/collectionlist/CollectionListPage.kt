package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.net.Uri
import com.fenchtose.movieratings.model.MovieCollection

interface CollectionListPage {

    fun updateState(state: State)
    fun updateState(state: OpState)
    fun updateState(state: ShareState)

    sealed class State {
        class Default: State()
        class Loading: State()
        class Error: State()
        class Success(val collections: ArrayList<MovieCollection>): State()
        class Empty: State()
    }

    sealed class OpState(val data: String) {
        class Created(data: String): OpState(data)
        class CreateError(data: String): OpState(data)
        class Deleted(data: String): OpState(data)
        class DeleteError(data: String): OpState(data)
    }

    sealed class ShareState {
        class Started: ShareState()
        class Error: ShareState()
        class Success(val uri: Uri): ShareState()
    }
}