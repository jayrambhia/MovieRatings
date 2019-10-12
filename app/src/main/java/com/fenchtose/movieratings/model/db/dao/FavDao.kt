package com.fenchtose.movieratings.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fenchtose.movieratings.model.db.entity.Fav

@Dao
interface FavDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fav: Fav)

    @Query("SELECT * FROM FAVS WHERE IMDBID LIKE :imdbId LIMIT 1")
    fun getFav(imdbId: String): Fav?

    @Query("DELETE FROM FAVS")
    fun deleteAll(): Int

    @Query("SELECT * FROM FAVS WHERE IS_FAV == 1")
    fun exportData(): List<Fav>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun importData(favs: List<Fav>): List<Long>

}