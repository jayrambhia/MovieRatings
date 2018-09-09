package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.convert
import io.reactivex.Observable

class DbFavoriteMovieProvider(private val movieDao: MovieDao) : FavoriteMovieProvider {

    override fun getMovies(): Observable<List<Movie>> {
        return Observable.fromCallable {
            movieDao.getFavMovies().convert()
        }.map {
            it.map { it.copy(liked = true) }
        }/*.doOnNext {
            it.map {
                it.liked = true
                it.appliedPreferences.liked = true
            }
        }*/
    }
}