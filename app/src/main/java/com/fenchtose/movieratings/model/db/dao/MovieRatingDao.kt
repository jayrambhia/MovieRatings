package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.entity.MovieRating

@Dao
interface MovieRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rating: MovieRating)

    @Query("SELECT * FROM MOVIE_RATINGS WHERE TITLE like :title")
    fun getRatingsForTitle(title: String): List<MovieRating>

    @Query("SELECT * FROM MOVIE_RATINGS WHERE TRANSLATED_TITLE like :title")
    fun getRatingsForTranslatedTitle(title: String): List<MovieRating>
}