package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import io.reactivex.Observable

interface MovieCollectionProvider {
    fun getCollections(withMovies:Boolean = false): Observable<List<MovieCollection>>
    fun getMoviesForCollection(collection: MovieCollection): Observable<List<Movie>>

    fun addPreferenceApplier(preferenceApplier: UserPreferenceApplier)
}