package com.fenchtose.movieratings.features.moviecollection.collectionlist

import com.fenchtose.movieratings.model.MovieCollection

interface CollectionListPage {

    fun updateState(state: State)

    data class State(val ui: Ui, val data: ArrayList<MovieCollection>? = null)

    enum class Ui {
        DEFAULT,
        LOADING,
        ERROR,
        DATA_LOADED,
        EMPTY,
        COLLECTION_CREATED,
        COLLECTION_CREATE_ERROR
    }
}