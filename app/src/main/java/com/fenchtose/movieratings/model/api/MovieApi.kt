package com.fenchtose.movieratings.model.api

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface MovieApi {
    @GET("/ ")
    fun getMovieInfo(@Query("apikey") apiKey: String, @Query("t") title: String, @Query("plot") plot: String = "full") : Observable<Movie>

    @GET("/ ")
    fun getMovieInfoWithImdb(@Query("apikey") apiKey: String, @Query("i") imdbId: String, @Query("plot") plot: String = "full") : Observable<Movie>

    @GET("/ ")
    fun search(@Query("apikey") apiKey: String, @Query("s") title: String) : Observable<SearchResult>
}