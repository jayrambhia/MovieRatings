package com.fenchtose.movieratings.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fenchtose.movieratings.model.db.entity.DisplayedRating

@Dao
interface DisplayedRatingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(rating: DisplayedRating)

    @Query("DELETE FROM DISPLAYED_RATINGS")
    fun deleteAll(): Int

    @Query("SELECT COUNT(id) FROM DISPLAYED_RATINGS")
    fun countAll(): Int

    @Query("SELECT COUNT(DISTINCT IMDBID) FROM DISPLAYED_RATINGS")
    fun countUnique(): Int
}