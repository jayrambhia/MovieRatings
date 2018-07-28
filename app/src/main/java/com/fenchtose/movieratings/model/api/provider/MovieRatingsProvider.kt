package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.MovieRating
import io.reactivex.Observable

interface MovieRatingsProvider {
    fun useFlutterApi(status: Boolean)
    fun getMovieRating(title: String, year: String?): Observable<MovieRating>
    fun report404(title: String, year: String?)
}