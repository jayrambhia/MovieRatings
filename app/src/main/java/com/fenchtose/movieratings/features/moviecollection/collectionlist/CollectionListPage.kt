package com.fenchtose.movieratings.features.moviecollection.collectionlist

import com.fenchtose.movieratings.model.MovieCollection

interface CollectionListPage {

    fun updateState(state: State)
    fun updateState(state: OpState)

    data class State(val ui: Ui, val data: ArrayList<MovieCollection>? = null)
    data class OpState(val op: Op, val data: String)

    enum class Op {
        COLLECTION_CREATED,
        COLLECTION_CREATE_ERROR,
        COLLECTION_DELETED,
        COLLECTION_DELETE_ERROR
    }

    enum class Ui {
        DEFAULT,
        LOADING,
        ERROR,
        DATA_LOADED,
        EMPTY
    }
}