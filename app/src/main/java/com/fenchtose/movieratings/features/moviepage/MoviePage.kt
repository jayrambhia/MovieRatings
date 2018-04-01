package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.model.EpisodesList
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection

interface MoviePage {
    fun updateState(state: State)
    fun updateState(state: CollectionState)
    fun updateState(state: EpisodeState)

    data class State(val ui: Ui, val movie: Movie? = null)
    data class CollectionState(val ui: CollectionUi, val collection: MovieCollection)
    data class EpisodeState(val ui: EpisodeUi, val season: EpisodesList? = null)

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

    enum class EpisodeUi {
        INVALID,
        LOADING,
        LOADED,
        ERROR
    }
}