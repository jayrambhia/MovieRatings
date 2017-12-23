package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.Movie

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSearch(movie: Movie)

    @Query("SELECT * FROM MOVIES WHERE TITLE LIKE :title LIMIT 1")
    fun getMovie(title: String): Movie?

    @Query("SELECT * FROM MOVIES as m INNER JOIN FAVS as f ON m.IMDBID == f.IMDBID WHERE f.IS_FAV == 1")
    fun getFavMovies(): List<Movie>
}