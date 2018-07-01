package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.entity.DisplayedRating

@Dao
interface DisplayedRatingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(rating: DisplayedRating)

    @Query("DELETE FROM DISPLAYED_RATINGS")
    fun deleteAll(): Int

    @Query("SELECT COUNT(id) FROM DISPLAYED_RATINGS")
    fun countAll(): Int

    @Query("SELECT COUNT(id) FROM DISPLAYED_RATINGS GROUP BY IMDBID")
    fun countUnique(): Int
}