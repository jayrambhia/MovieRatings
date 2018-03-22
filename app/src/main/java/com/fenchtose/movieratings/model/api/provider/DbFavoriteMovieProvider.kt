package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable

class DbFavoriteMovieProvider(private val movieDao: MovieDao) : FavoriteMovieProvider {

    override fun getMovies(): Observable<ArrayList<Movie>> {
        return Observable.fromCallable {
            movieDao.getFavMovies() as ArrayList
        }.doOnNext {
            it.map {
                it.liked = true
                it.appliedPreferences.liked = true
            }
        }
    }
}