package com.fenchtose.movieratings.features.searchpage

import com.fenchtose.movieratings.model.Movie

interface SearchPage {
    fun updateState(state: State)

    enum class Ui {
        DEFAULT,
        LOADING,
        ERROR,
        DATA_LOADED
    }

    data class State(val ui: Ui, val movies: ArrayList<Movie> = ArrayList())
}