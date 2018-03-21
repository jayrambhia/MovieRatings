package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
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
}