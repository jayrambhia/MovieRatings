package com.fenchtose.movieratings.features.moviecollection.collectionlist

import android.net.Uri
import com.fenchtose.movieratings.model.MovieCollection

interface CollectionListPage {

    fun updateState(state: State)
    fun updateState(state: OpState)
    fun updateState(state: ShareState)

    sealed class OpState(val data: String) {
        class Created(data: String): OpState(data)
        class CreateError(data: String): OpState(data)
        class Deleted(data: String): OpState(data)
        class DeleteError(data: String): OpState(data)
    }

    sealed class ShareState {
        object Started: ShareState()
        object Error: ShareState()
        class Success(val uri: Uri): ShareState()
    }

    sealed class State {
        object Default: State()
        object Loading: State()
        object Error: State()
        class Success(val collections: ArrayList<MovieCollection>): State()
        object Empty: State()
    }

}