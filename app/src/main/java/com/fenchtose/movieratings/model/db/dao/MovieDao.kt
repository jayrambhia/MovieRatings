package com.fenchtose.movieratings.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fenchtose.movieratings.model.db.entity.CollectedMovie
import com.fenchtose.movieratings.model.db.entity.Episode
import com.fenchtose.movieratings.model.db.entity.Movie
import com.fenchtose.movieratings.model.db.entity.RecentlyBrowsedMovie

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(episode: Episode): Long

    @Query("SELECT * FROM EPISODES WHERE SERIESIMDBID = :seriesImdbId AND SEASON = :season")
    fun getEpisodesForSeason(seriesImdbId: String, season: Int): List<Episode>

    @Query("SELECT * FROM MOVIES as m INNER JOIN EPISODES as e ON m.IMDBID == e.IMDBID WHERE e.SERIESIMDBID = :seriesImdbId AND e.season = :season AND e.episode = :episode")
    fun getEpisode(seriesImdbId: String, season: Int, episode: Int): Movie?

    @Query("SELECT * FROM MOVIES WHERE IMDBID IN(:ids)")
    fun exportData(ids: List<String>): List<Movie>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun importData(movies: List<Movie>): List<Long>
}