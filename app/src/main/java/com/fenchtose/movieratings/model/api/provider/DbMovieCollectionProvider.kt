package com.fenchtose.movieratings.model.api.provider

import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.db.dao.MovieCollectionDao
import io.reactivex.Observable

class DbMovieCollectionProvider(private val dao: MovieCollectionDao) : MovieCollectionProvider {

    override fun getCollections(): Observable<List<MovieCollection>> {
        return Observable.defer {
            Observable.just(dao.getMovieCollections())
        }
    }
}