package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Trending
import io.reactivex.Observable

interface MovieRatingsProvider {
    fun useFlutterApi(status: Boolean)
    fun getMovieRating(request: RatingRequest): Observable<MovieRating>
    fun report404(title: String, year: String?)
    fun getTrending(): Observable<Trending>
}

data class RatingRequest(val title: String, val year: String?, val appName: String)