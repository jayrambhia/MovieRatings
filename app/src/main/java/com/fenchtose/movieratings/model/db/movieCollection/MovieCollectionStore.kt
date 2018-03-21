package com.fenchtose.movieratings.model.db.movieCollection

import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.MovieCollectionEntry
import io.reactivex.Observable

interface MovieCollectionStore {
    fun createCollection(name: String): Observable<MovieCollection>
    fun addMovieToCollection(collection: MovieCollection, movie: Movie): Observable<MovieCollectionEntry>
}