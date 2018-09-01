package com.fenchtose.movieratings.features.moviepage

import com.fenchtose.movieratings.model.entity.Season
import com.fenchtose.movieratings.model.db.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.Movie

interface MoviePage {
    fun updateState(state: State)
    fun updateState(state: CollectionState)
    fun updateState(state: EpisodeState)

    sealed class State {
        class Loading: State()
        class Success(val movie: Movie): State()
        class LoadImage(val image: String): State()
        class Error: State()
    }

    sealed class CollectionState(val collection: MovieCollection) {
        class Exists(collection: MovieCollection): CollectionState(collection)
        class Added(collection: MovieCollection): CollectionState(collection)
        class Error(collection: MovieCollection): CollectionState(collection)
    }

    sealed class EpisodeState {
        class Loading: EpisodeState()
        class Success(val season: Season): EpisodeState()
        class Error: EpisodeState()
        class Invalid: EpisodeState()
    }

}