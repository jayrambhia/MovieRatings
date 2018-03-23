package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import com.fenchtose.movieratings.model.db.dao.MovieCollectionDao
import io.reactivex.Observable

class DbMovieCollectionProvider(private val dao: MovieCollectionDao) : MovieCollectionProvider {

    private val preferenceAppliers = ArrayList<UserPreferneceApplier>()

    override fun getCollections(): Observable<List<MovieCollection>> {
        return Observable.defer {
            Observable.just(dao.getMovieCollections())
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

    override fun addPreferenceApplier(preferenceApplier: UserPreferneceApplier) {
        preferenceAppliers.add(preferenceApplier)
    }
}