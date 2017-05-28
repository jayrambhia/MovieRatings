package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.Movie

@Dao
interface MovieDao {
    @Insert
    fun insert(movie: Movie)

    @Query("SELECT * FROM MOVIES WHERE TITLE LIKE :arg0 LIMIT 1")
    fun getMovie(title: String): Movie?
}