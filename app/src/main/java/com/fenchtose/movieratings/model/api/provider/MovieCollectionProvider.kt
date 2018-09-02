package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import io.reactivex.Observable

interface MovieCollectionProvider {
    fun getCollections(withMovies:Boolean = false): Observable<List<MovieCollection>>
    fun getMoviesForCollection(collection: MovieCollection): Observable<List<Movie>>

    fun addPreferenceApplier(preferenceApplier: UserPreferenceApplier)
}