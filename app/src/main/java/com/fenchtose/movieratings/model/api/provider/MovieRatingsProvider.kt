package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.MovieRating
import com.fenchtose.movieratings.model.entity.Trending
import io.reactivex.Observable

interface MovieRatingsProvider {
    fun useFlutterApi(status: Boolean)
    fun getMovieRating(request: RatingRequest): Observable<MovieRating>
    fun report404(title: String, year: String?)
    fun getTrending(period: String): Observable<Trending>
}

const val ORDER_POPULAR = "popular"
const val ORDER_RECENT = "recent"

data class RatingRequest(
        val title: String,
        val year: String?,
        val order: String="popular",
        val checkAnime: Int = 0,
        val appName: String)