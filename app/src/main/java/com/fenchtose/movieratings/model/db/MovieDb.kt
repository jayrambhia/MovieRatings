package com.fenchtose.movieratings.model.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
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

            instance = Room.databaseBuilder(context, MovieDb::class.java, "ex")
                    .addMigrations(MIGRATION_1to_2)
                    .build()
            return instance!!
        }

        private val MIGRATION_1to_2 = object: Migration(1, 2) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                // Drop old tables

                _db.execSQL("DROP TABLE IF EXISTS `MOVIES`")
                _db.execSQL("DROP TABLE IF EXISTS `FAVS`")

                // Add tables again
                _db.execSQL("CREATE TABLE IF NOT EXISTS `MOVIES` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `POSTER` TEXT NOT NULL, `RATINGS` TEXT NOT NULL, `TYPE` TEXT NOT NULL, `IMDBID` TEXT NOT NULL, `YEAR` TEXT NOT NULL, `RATED` TEXT NOT NULL, `RELEASED` INTEGER NOT NULL, `RUNTIME` TEXT NOT NULL, `GENRE` TEXT NOT NULL, `DIRECTOR` TEXT NOT NULL, `WRITERS` TEXT NOT NULL, `ACTORS` TEXT NOT NULL, `PLOT` TEXT NOT NULL, `LANGUAGE` TEXT NOT NULL, `COUNTRY` TEXT NOT NULL, `AWARDS` TEXT NOT NULL, `IMDBVOTES` TEXT NOT NULL, `PRODUCTION` TEXT NOT NULL, `WEBSITE` TEXT NOT NULL)")
                _db.execSQL("CREATE UNIQUE INDEX `index_MOVIES_IMDBID` ON `MOVIES` (`IMDBID`)")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `FAVS` (`IMDBID` TEXT NOT NULL, `IS_FAV` INTEGER NOT NULL, PRIMARY KEY(`IMDBID`))")
                _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
                _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"06cf262ae10bd31d52a98bdd65653276\")")
            }
        }
    }
}