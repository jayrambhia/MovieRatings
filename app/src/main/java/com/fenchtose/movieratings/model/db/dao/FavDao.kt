package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.Fav

@Dao
interface FavDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fav: Fav)

    @Query("SELECT * FROM FAVS WHERE IMDBID LIKE :imdbId LIMIT 1")
    fun getFav(imdbId: String): Fav?

    @Query("DELETE FROM FAVS")
    fun deleteAll(): Int

    @Query("SELECT * FROM FAVS")
    fun getAll(): List<Fav>

}