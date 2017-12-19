package com.fenchtose.movieratings.model.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.fenchtose.movieratings.model.Fav
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.db.dao.FavDao
import com.fenchtose.movieratings.model.db.dao.MovieDao

@Database(entities = arrayOf(Movie::class, Fav::class), version = 2)
abstract class MovieDb : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun favDao(): FavDao

    companion object {
        var instance: MovieDb? = null

        fun getInstance(context: Context) : MovieDb {
            instance?.let {
                return instance as MovieDb
            }

            instance = Room.databaseBuilder(context, MovieDb::class.java, "ex").build()
            return instance!!
        }
    }
}