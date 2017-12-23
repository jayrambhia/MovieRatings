package com.fenchtose.movieratings.model.api.provider

import android.content.Context
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.SearchResult
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable


class PreloadedMovieProvider(context:Context, private val dao: MovieDao): MovieProvider {

    override fun getMovieWithImdb(imdb: String): Observable<Movie> {
        throw RuntimeException("PreloadedMovieProvider should not be used in release")
    }

    override fun getMovie(title: String): Observable<Movie> {
        throw RuntimeException("PreloadedMovieProvider should not be used in release")
    }

    override fun search(title: String): Observable<SearchResult> {
        throw RuntimeException("PreloadedMovieProvider should not be used in release")
    }

    override fun addPreferenceApplier(applier: UserPreferneceApplier) {
        throw RuntimeException("PreloadedMovieProvider should not be used in release")
    }
}