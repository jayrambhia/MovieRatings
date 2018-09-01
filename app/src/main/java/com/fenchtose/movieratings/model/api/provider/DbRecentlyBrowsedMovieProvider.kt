package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.db.apply
import com.fenchtose.movieratings.model.db.dao.MovieDao
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.convert
import io.reactivex.Observable

class DbRecentlyBrowsedMovieProvider(private val movieDao: MovieDao): RecentlyBrowsedMovieProvider {

    private val preferenceAppliers = ArrayList<UserPreferenceApplier>()

    override fun getMovies(): Observable<List<Movie>> {
        return Observable.fromCallable {
            movieDao.getRecentlyBrowsedMovies()
        }.map {
            val movies: ArrayList<Movie> = ArrayList()
            it.forEach {
                it.movies?.let {
                    movies.addAll(it.convert())
                }
            }

            movies
        }.map {
            it.map { preferenceAppliers.apply(it) }
        }

    }

    override fun addPreferenceApplier(applier: UserPreferenceApplier) {
        preferenceAppliers.add(applier)
    }
}