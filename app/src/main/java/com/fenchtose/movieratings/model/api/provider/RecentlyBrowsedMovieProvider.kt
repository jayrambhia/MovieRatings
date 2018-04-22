package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.RecentlyBrowsedMovie
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import io.reactivex.Observable

interface RecentlyBrowsedMovieProvider {
    fun getMovies(): Observable<List<RecentlyBrowsedMovie>>
    fun addPreferenceApplier(applier: UserPreferenceApplier)
}