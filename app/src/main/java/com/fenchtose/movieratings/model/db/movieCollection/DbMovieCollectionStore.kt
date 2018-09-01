package com.fenchtose.movieratings.model.db.movieCollection

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.db.entity.MovieCollection
import com.fenchtose.movieratings.model.db.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.dao.MovieCollectionDao
import com.fenchtose.movieratings.model.entity.Movie
import io.reactivex.Observable
import io.reactivex.Single
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
                        it.id = dao.insert(it)
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
            Observable.just(MovieCollectionEntry.create(collection, movie.imdbId))
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
    override fun apply(movie: Movie): Movie {
        return movie.copy(collections = dao.getCollectionsForMovie(movie.imdbId).sortedBy { it.name },
                preferences = movie.preferences.copy(collections = true))
    }

    override fun export(): Single<List<MovieCollection>> {
        return Single.defer {
            Single.fromCallable { dao.getMovieCollections() }
                    .map {
                        it.map {
                            it.entries = dao.getCollectionEntries(it.id)
                        }
                        it
                    }
        }
    }

    override fun export(collectionId: Long): Single<List<MovieCollection>> {
        return Single.defer {
            Single.fromCallable {
                dao.getMovieCollection(collectionId) ?: MovieCollection.invalid()
            }.map {
                if (it.id != -1L) {
                    it.entries = dao.getCollectionEntries(it.id)
                    arrayListOf(it)
                } else {
                    ArrayList()
                }
            }
        }
    }

    @WorkerThread
    override fun import(collections: List<MovieCollection>): Int {
        var totalEntries = 0

        collections.filter {
            it.entries.isNotEmpty()
        }.run {
            for (collection in this) {
                val existingCollection = dao.findCollectionByName(collection.name)
                val collectionId = existingCollection?.id ?: dao.insert(MovieCollection.create(collection.name))
                if (collectionId != -1L) {
                    val existingEntries = dao.getCollectionEntries(collectionId).map { it.movieId }
                    collection.entries.filter {
                        it.movieId !in existingEntries
                    }.map {
                        MovieCollectionEntry.create(collectionId, it.movieId)
                    }
                    .run {
                        totalEntries += dao.importEntries(this).filter { it != -1L }.count()
                    }
                }
            }
        }

        return totalEntries
    }
}