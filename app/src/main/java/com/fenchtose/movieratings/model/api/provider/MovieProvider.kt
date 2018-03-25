package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import io.reactivex.Observable

interface MovieProvider {
    fun getMovieWithImdb(imdbId: String): Observable<Movie>
    fun getMovie(title: String, year: String): Observable<Movie>
    fun search(title: String, page:Int = 1): Observable<SearchResult>
    fun addPreferenceApplier(applier: UserPreferneceApplier)
}