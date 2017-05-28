package com.fenchtose.movieratings.model.api

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface MovieApi {
    @GET("/ ")
    fun getMovieInfo(@Query("t") title: String, @Query("apikey") api: String) : Observable<Movie>

    @GET("/ ")
    fun search(@Query("s") title: String, @Query("apikey") api: String) : Observable<SearchResult>
}