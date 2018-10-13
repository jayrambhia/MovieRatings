package com.fenchtose.movieratings.model.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import com.fenchtose.movieratings.MovieRatingsApplication
import com.fenchtose.movieratings.model.db.dao.*
import com.fenchtose.movieratings.model.db.entity.*

@Database(entities = [
    (Movie::class), (Fav::class),
    (RecentlyBrowsed::class), (MovieCollection::class),
    (MovieCollectionEntry::class), (Episode::class),
    (DisplayedRating::class), (MovieRating::class),
    (RatingNotFound::class)],
        version = 9)
abstract class MovieDb : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun favDao(): FavDao
    abstract fun recentlyBrowsedDao(): RecentlyBrowsedDao
    abstract fun movieCollectionDao(): MovieCollectionDao
    abstract fun displayedRatingsDao(): DisplayedRatingDao
    abstract fun movieRatingsDao(): MovieRatingDao

    companion object {
        val instance: MovieDb by lazy {
            Room.databaseBuilder(MovieRatingsApplication.instance, MovieDb::class.java, "ex")
                    .addMigrations(
                            MIGRATION_1_to_2, MIGRATION_2_to_3,
                            MIGRATION_3_to_4, MIGRATION_4_to_5,
                            MIGRATION_5_to_6, MIGRATION_6_to_7,
                            MIGRATION_7_to_8, MIGRATION_8_to_9)
                    .build()
        }

        private val MIGRATION_1_to_2 = object: Migration(1, 2) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                // Drop old tables

                _db.execSQL("DROP TABLE IF EXISTS `MOVIES`")
                _db.execSQL("DROP TABLE IF EXISTS `FAVS`")

                // Add tables again
                _db.execSQL("CREATE TABLE IF NOT EXISTS `MOVIES` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `POSTER` TEXT NOT NULL, `RATINGS` TEXT NOT NULL, `TYPE` TEXT NOT NULL, `IMDBID` TEXT NOT NULL, `YEAR` TEXT NOT NULL, `RATED` TEXT NOT NULL, `RELEASED` TEXT NOT NULL, `RUNTIME` TEXT NOT NULL, `GENRE` TEXT NOT NULL, `DIRECTOR` TEXT NOT NULL, `WRITERS` TEXT NOT NULL, `ACTORS` TEXT NOT NULL, `PLOT` TEXT NOT NULL, `LANGUAGE` TEXT NOT NULL, `COUNTRY` TEXT NOT NULL, `AWARDS` TEXT NOT NULL, `IMDBVOTES` TEXT NOT NULL, `PRODUCTION` TEXT NOT NULL, `WEBSITE` TEXT NOT NULL)")
                _db.execSQL("CREATE UNIQUE INDEX `index_MOVIES_IMDBID` ON `MOVIES` (`IMDBID`)")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `FAVS` (`IMDBID` TEXT NOT NULL, `IS_FAV` INTEGER NOT NULL, PRIMARY KEY(`IMDBID`))")
                _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
                _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"06cf262ae10bd31d52a98bdd65653276\")")
            }
        }

        private val MIGRATION_2_to_3 = object: Migration(2, 3) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `RECENTLY_BROWSED` (`IMDBID` TEXT NOT NULL, `TIMESTAMP` INTEGER NOT NULL, PRIMARY KEY(`IMDBID`))")
            }
        }

        private val MIGRATION_3_to_4 = object: Migration(3, 4) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `COLLECTIONS` (`COLLECTION_ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `COLLECTION_NAME` TEXT NOT NULL, `CREATED_AT` INTEGER NOT NULL, `UPDATED_AT` INTEGER NOT NULL, `IS_DELETED` INTEGER NOT NULL)")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `COLLECTION_ENTRIES` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `COLLECTION_ID` INTEGER NOT NULL, `IMDBID` TEXT NOT NULL, `CREATED_AT` INTEGER NOT NULL, `UPDATED_AT` INTEGER NOT NULL, `IS_DELETED` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_4_to_5 = object: Migration(4, 5) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("ALTER TABLE `MOVIES` ADD COLUMN `TOTALSEASONS` INTEGER DEFAULT -1 NOT NULL")
                _db.execSQL("CREATE TABLE IF NOT EXISTS `EPISODES` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `RELEASED` TEXT NOT NULL, `EPISODE` INTEGER NOT NULL, `IMDBRATING` TEXT NOT NULL, `IMDBID` TEXT NOT NULL, `SERIESIMDBID` TEXT NOT NULL, `SEASON` INTEGER NOT NULL)")
                _db.execSQL("CREATE UNIQUE INDEX `index_EPISODES_IMDBID` ON `EPISODES` (`IMDBID`)")
            }
        }

        private val MIGRATION_5_to_6 = object: Migration(5, 6) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `DISPLAYED_RATINGS` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `IMDBID` TEXT NOT NULL, `TIMESTAMP` INTEGER NOT NULL, `APP_PACKAGE` TEXT NOT NULL)");
            }
        }

        private val MIGRATION_6_to_7 = object: Migration(6, 7) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `MOVIE_RATINGS` (`IMDBID` TEXT NOT NULL, `IMDB_RATING` REAL NOT NULL, `IMDB_VOTES` INTEGER NOT NULL, `TITLE` TEXT NOT NULL, `TITLE_TYPE` TEXT NOT NULL, `TRANSLATED_TITLE` TEXT NOT NULL, `START_YEAR` INTEGER NOT NULL, `END_YEAR` INTEGER NOT NULL, `TIMESTAMP` INTEGER NOT NULL, PRIMARY KEY(`IMDBID`))")
                _db.execSQL("CREATE UNIQUE INDEX `index_MOVIE_RATINGS_IMDBID` ON `MOVIE_RATINGS` (`IMDBID`)")
            }
        }

        private val MIGRATION_7_to_8 = object: Migration(7, 8) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("CREATE TABLE IF NOT EXISTS `RATING_NOT_FOUND` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `TITLE` TEXT NOT NULL, `YEAR` TEXT NOT NULL, `TIMESTAMP` INTEGER NOT NULL)")
                _db.execSQL("CREATE  INDEX `index_RATING_NOT_FOUND_TITLE` ON `RATING_NOT_FOUND` (`TITLE`)")
            }
        }

        private val MIGRATION_8_to_9 = object: Migration(8, 9) {
            override fun migrate(_db: SupportSQLiteDatabase) {
                _db.execSQL("ALTER TABLE `MOVIE_RATINGS` ADD COLUMN `SOURCE` TEXT DEFAULT \"IMDB\" NOT NULL")
            }
        }
    }
}