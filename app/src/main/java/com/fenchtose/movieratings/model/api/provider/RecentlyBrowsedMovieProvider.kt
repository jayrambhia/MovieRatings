package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable

interface RecentlyBrowsedMovieProvider {
    fun getMovies(): Observable<List<Movie>>
    fun addPreferenceApplier(applier: UserPreferenceApplier)
}