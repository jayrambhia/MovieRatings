package com.fenchtose.movieratings.features.baselistpage

import com.fenchtose.movieratings.model.Movie

interface BaseMovieListPage {

    fun updateState(state: State)

    data class State(val ui: Ui, val data: ArrayList<Movie>?)

    enum class Ui {
        LOADING,
        DATA_LOADED,
        EMPTY,
        ERROR
    }
}