package com.fenchtose.movieratings.model.db.movieCollection

import android.support.annotation.WorkerThread
import com.fenchtose.movieratings.model.entity.Movie
import com.fenchtose.movieratings.model.entity.MovieCollection
import com.fenchtose.movieratings.model.entity.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.UserPreferenceApplier
import io.reactivex.Observable
import io.reactivex.Single

interface MovieCollectionStore : UserPreferenceApplier {
    fun createCollection(name: String): Observable<MovieCollection>
    fun deleteCollection(collection: MovieCollection): Observable<Boolean>
    fun addMovieToCollection(collection: MovieCollection, movie: Movie): Observable<MovieCollectionEntry>
    fun isMovieAddedToCollection(collection: MovieCollection, movie: Movie): Observable<Boolean>
    fun removeMovieFromCollection(collection: MovieCollection, movie: Movie): Observable<Boolean>
    fun deleteAllCollectionEntries(): Observable<Int>
    fun deleteAllCollections(): Observable<Int>
    fun export(): Single<List<MovieCollection>>
    fun export(collectionId: Long): Single<List<MovieCollection>>
    @WorkerThread
    fun import(collections: List<MovieCollection>): Int
}