package com.fenchtose.movieratings.model.db.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.fenchtose.movieratings.model.CollectedMovie
import com.fenchtose.movieratings.model.Movie
import com.fenchtose.movieratings.model.MovieCollectionEntry
import com.fenchtose.movieratings.model.RecentlyBrowsedMovie

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSearch(movie: Movie)

    @Query("SELECT * FROM MOVIES WHERE TITLE LIKE :title LIMIT 1")
    fun getMovie(title: String): Movie?

    @Query("SELECT * FROM MOVIES WHERE TITLE LIKE :title AND YEAR = :year LIMIT 1")
    fun getMovie(title: String, year: String): Movie?

    @Query("SELECT * FROM MOVIES WHERE IMDBID = :imdbId")
    fun getMovieWithImdbId(imdbId: String): Movie?

    @Query("SELECT * FROM MOVIES as m INNER JOIN FAVS as f ON m.IMDBID == f.IMDBID WHERE f.IS_FAV == 1")
    fun getFavMovies(): List<Movie>

    @Query("SELECT * FROM RECENTLY_BROWSED ORDER BY TIMESTAMP DESC")
    fun getRecentlyBrowsedMovies(): List<RecentlyBrowsedMovie>

    @Query("SELECT * FROM COLLECTION_ENTRIES WHERE COLLECTION_ID = :collectionId")
    fun getCollectionMovies(collectionId: String): List<CollectedMovie>
}