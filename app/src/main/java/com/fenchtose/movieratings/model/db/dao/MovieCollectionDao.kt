package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollection
import com.fenchtose.movieratings.model.MovieCollectionEntry

@Dao
interface MovieCollectionDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insert(collection: MovieCollection)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(entry: MovieCollectionEntry)

    @Query("SELECT * FROM COLLECTIONS")
    fun getMovieCollections(): List<MovieCollection>

    @Query("SELECT CASE WHEN EXISTS (" +
            " SELECT * FROM COLLECTION_ENTRIES WHERE COLLECTION_ID = :collectionId AND IMDBID = :imdbId LIMIT 1" +
            ")" +
            "THEN CAST(1 AS BIT)" +
            "ELSE CAST(0 AS BIT) END")
    fun isMovieAddedToCollection(collectionId: Long, imdbId: String): Boolean

    @Query("SELECT * FROM COLLECTIONS as c INNER JOIN COLLECTION_ENTRIES as ce ON c.COLLECTION_ID == ce.COLLECTION_ID WHERE ce.IMDBID == :imdbId")
    fun getCollectionsForMovie(imdbId: String): List<MovieCollection>

    @Query("SELECT * FROM MOVIES as m INNER JOIN COLLECTION_ENTRIES as ce ON m.IMDBID == ce.IMDBID WHERE ce.COLLECTION_ID == :collectionId")
    fun getMoviesForCollection(collectionId: Long): List<Movie>

}