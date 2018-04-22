package com.fenchtose.movieratings.model.db.movieCollection

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.dao.MovieCollectionDao
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class DbMovieCollectionStore private constructor(private val dao: MovieCollectionDao) : MovieCollectionStore {

    companion object {
        private var instance: DbMovieCollectionStore? = null
        fun getInstance(dao: MovieCollectionDao): DbMovieCollectionStore {
            if (instance == null) {
                instance = DbMovieCollectionStore(dao)
            }

            return instance!!
        }
    }

    override fun createCollection(name: String): Observable<MovieCollection> {
        return Observable.defer {
            Observable.just(name)
                    .map {
                        name -> MovieCollection.create(name)
                    }
                    .doOnNext {
                        dao.insert(it)
                    }
        }
    }

    override fun deleteCollection(collection: MovieCollection): Observable<Boolean> {
        return Observable.defer {
            Observable.zip(
                    Observable.just(dao.deleteCollectionEntries(collection.id)),
                    Observable.just(dao.delete(collection)),
                    BiFunction<Int, Int, Boolean> { _, t2 ->  t2 > 0}
                    )
        }
    }

    override fun addMovieToCollection(collection: MovieCollection, movie: Movie): Observable<MovieCollectionEntry> {
        return Observable.defer {
            Observable.just(MovieCollectionEntry.create(collection, movie))
                    .doOnNext {
                        dao.insert(it)
                    }
        }
    }

    override fun isMovieAddedToCollection(collection: MovieCollection, movie: Movie): Observable<Boolean> {
        return Observable.defer {
            Observable.just(dao.isMovieAddedToCollection(collection.id, movie.imdbId))
        }
    }

    override fun removeMovieFromCollection(collection: MovieCollection, movie: Movie): Observable<Boolean> {
        return Observable.defer {
            Observable.just(dao.deleteCollectionEntry(collection.id, movie.imdbId))
                    .map { it == 1 }
        }
    }

    override fun deleteAllCollectionEntries(): Observable<Int> = Observable.defer {
        Observable.just(dao.deleteAllCollectionEntries())
    }

    override fun deleteAllCollections(): Observable<Int> = Observable.defer {
        Observable.just(dao.deleteAllCollections())
    }

    @WorkerThread
    override fun apply(movie: Movie) {
        movie.collections = dao.getCollectionsForMovie(movie.imdbId).sortedBy { it.name }
        movie.appliedPreferences.collections = true
    }
}