package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable

interface FavoriteMovieProvider {
    fun getMovies(): Observable<ArrayList<Movie>>
}