package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.RecentlyBrowsedMovie
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieDao
import io.reactivex.Observable

class DbRecentlyBrowsedMovieProvider(private val movieDao: MovieDao): RecentlyBrowsedMovieProvider {

    private val preferenceAppliers = ArrayList<UserPreferneceApplier>()

    override fun getMovies(): Observable<List<RecentlyBrowsedMovie>> {
        return Observable.fromCallable {
            movieDao.getRecentlyBrowsedMovies()
                    .filter {
                        it.movies != null && it.movies!!.isNotEmpty()
                    }
            }.doOnNext {
                it.map {
                    it.movies?.takeIf { it.isNotEmpty() }?.let {
                        it.map {
                            for (preferenceApplier in preferenceAppliers) {
                                preferenceApplier.apply(it)
                            }
                        }
                    }
                }

            }
    }

    override fun addPreferenceApplier(applier: UserPreferneceApplier) {
        preferenceAppliers.add(applier)
    }
}