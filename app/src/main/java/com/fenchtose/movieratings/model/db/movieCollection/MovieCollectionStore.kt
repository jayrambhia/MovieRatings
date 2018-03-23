package com.fenchtose.movieratings.model.db.movieCollection

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.MovieCollectionEntry
import com.fenchtose.movieratings.model.db.UserPreferneceApplier
import io.reactivex.Observable

interface MovieCollectionStore : UserPreferneceApplier {
    fun createCollection(name: String): Observable<MovieCollection>
    fun deleteCollection(collection: MovieCollection): Observable<Boolean>
    fun addMovieToCollection(collection: MovieCollection, movie: Movie): Observable<MovieCollectionEntry>
    fun isMovieAddedToCollection(collection: MovieCollection, movie: Movie): Observable<Boolean>
}