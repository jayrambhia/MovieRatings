package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.RecentlyBrowsedMovie
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable

class DbRecentlyBrowsedMovieProvider(private val movieDao: MovieDao): RecentlyBrowsedMovieProvider {

    override fun getMovies(): Observable<List<RecentlyBrowsedMovie>> {
        return Observable.fromCallable {
            movieDao.getRecentlyBrowsedMovies()
                    .filter {
                        it.movies != null && it.movies!!.isNotEmpty()
                    }
        }
    }
}