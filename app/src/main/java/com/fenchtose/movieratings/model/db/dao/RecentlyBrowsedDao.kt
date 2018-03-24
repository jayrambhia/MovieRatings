package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.RecentlyBrowsed

@Dao
interface RecentlyBrowsedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: RecentlyBrowsed)

    @Query("DELETE FROM RECENTLY_BROWSED")
    fun deleteAll(): Int
}