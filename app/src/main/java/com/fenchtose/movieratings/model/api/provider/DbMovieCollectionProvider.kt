package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import com.fenchtose.movieratings.model.db.dao.MovieCollectionDao
import io.reactivex.Observable

class DbMovieCollectionProvider(private val dao: MovieCollectionDao) : MovieCollectionProvider {

    private val preferenceAppliers = ArrayList<UserPreferenceApplier>()

    override fun getCollections(withMovies: Boolean): Observable<List<MovieCollection>> {
        return Observable.defer {
            Observable.just(dao.getMovieCollections())
                    .map {
                        if (withMovies) {
                            it.forEach {
                                val movies = dao.getMoviesForCollection(it.id)
                                @Suppress("SENSELESS_COMPARISON")
                                if (movies != null) {
                                    it.movies = movies
                                }
                            }
                        }
                        it
                    }
        }
    }

    override fun getMoviesForCollection(collection: MovieCollection): Observable<List<Movie>> {
        return Observable.defer {
            Observable.just(dao.getMoviesForCollection(collection.id))
                    .doOnNext {
                        it.map {
                            preferenceAppliers.forEach {
                                applier -> applier.apply(it)
                            }
                        }
                    }
        }
    }

    override fun addPreferenceApplier(preferenceApplier: UserPreferenceApplier) {
        preferenceAppliers.add(preferenceApplier)
    }
}