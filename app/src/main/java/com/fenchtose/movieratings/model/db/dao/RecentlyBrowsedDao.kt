package com.fenchtose.movieratings.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fenchtose.movieratings.model.db.entity.RecentlyBrowsed

@Dao
interface RecentlyBrowsedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: RecentlyBrowsed)

    @Query("DELETE FROM RECENTLY_BROWSED")
    fun deleteAll(): Int

    @Query("SELECT * FROM RECENTLY_BROWSED")
    fun getAll(): List<RecentlyBrowsed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun importData(data: List<RecentlyBrowsed>): List<Long>
}