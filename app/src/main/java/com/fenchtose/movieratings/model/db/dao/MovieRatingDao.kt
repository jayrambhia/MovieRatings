package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.db.entity.MovieRating
import com.fenchtose.movieratings.model.db.entity.RatingNotFound

@Dao
interface MovieRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rating: MovieRating)

    @Query("SELECT * FROM MOVIE_RATINGS WHERE TITLE like :title")
    fun getRatingsForTitle(title: String): List<MovieRating>

    @Query("SELECT * FROM MOVIE_RATINGS WHERE TRANSLATED_TITLE like :title")
    fun getRatingsForTranslatedTitle(title: String): List<MovieRating>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rating: RatingNotFound)

    @Query("SELECT COUNT(id) FROM RATING_NOT_FOUND WHERE TITLE like :title AND TIMESTAMP > :timestamp AND year = ''")
    fun get404ForTitle(title: String, timestamp: Long): Long

    @Query("SELECT COUNT(id) FROM RATING_NOT_FOUND WHERE TITLE like :title AND TIMESTAMP > :timestamp AND year = :year")
    fun get404ForTitle(title: String, timestamp: Long, year: String): Long
}