package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.RecentlyBrowsedMovie
import io.reactivex.Observable

interface RecentlyBrowsedMovieProvider {
    fun getMovies(): Observable<List<RecentlyBrowsedMovie>>
}