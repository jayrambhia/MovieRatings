package com.fenchtose.movieratings.model.api

import com.fenchtose.movieratings.model.entity.MovieRating
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieRatingApi {
    @GET("/rating")
    fun getMovieRating(@Query("title") title: String,
                       @Query("year") year: String? = null,
                       @Query("type") type: String? = null): Observable<MovieRating>
}