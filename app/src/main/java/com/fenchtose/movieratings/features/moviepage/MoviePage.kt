package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection

interface MoviePage {
    fun updateState(state: State)
    fun updateState(state: CollectionState)
    fun addToCollection()

    data class State(val ui: Ui, val movie: Movie? = null)
    data class CollectionState(val ui: CollectionUi, val collection: MovieCollection)

    enum class Ui {
        LOADING,
        LOADED,
        LOAD_IMAGE,
        ERROR
    }

    enum class CollectionUi {
        EXISTS,
        ADDED,
        ERROR
    }
}