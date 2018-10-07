package com.fenchtose.movieratings.model.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.entity.OmdbMovie
import com.fenchtose.movieratings.util.FixTitleUtils

@Entity(tableName = "MOVIE_RATINGS", indices = arrayOf(Index("IMDBID", unique = true)))
data class MovieRating(
        @PrimaryKey
        @ColumnInfo(name = "IMDBID")
        val imdbId: String,

        @ColumnInfo(name = "IMDB_RATING")
        val rating: Float,

        @ColumnInfo(name = "IMDB_VOTES")
        val votes: Int,

        @ColumnInfo(name = "TITLE")
        val title: String,

        @ColumnInfo(name = "TITLE_TYPE")
        val type: String,

        @ColumnInfo(name = "TRANSLATED_TITLE")
        val translatedTitle: String,

        @ColumnInfo(name = "START_YEAR")
        val startYear: Int,

        @ColumnInfo(name = "END_YEAR")
        val endYear: Int = -1,

        @ColumnInfo(name = "TIMESTAMP")
        val timestamp: Int = -1
) {

    fun fitsYear(year: Int): Boolean {
        if (year != -1) {
            if (startYear == -1 && endYear == -1) {
                return false
            }

            if (startYear == -1) {
                return endYear == year
            }

            if (endYear == -1) {
                if (type == "tvSeries") {
                    // might be still running
                    return startYear <= year
                }

                return startYear == year
            }

            return year in startYear..endYear
        }

        return true
    }


    /*fun toMovie(): Movie {
        val movie = Movie()
        movie.imdbId = imdbId
        movie.title = title
        movie.type = type

        if (startYear != -1 && endYear != -1) {
            movie.year = "$startYear - $endYear"
        } else if (startYear != -1) {
            movie.year = startYear.toString()
        }

        if (votes > 0) {
            movie.imdbVotes = votes.toString()
        }

        if (rating > 0) {
            movie.ratings = arrayListOf(Rating("Internet Movie Database", "$rating/10"))
        }

        movie.poster = "http://img.omdbapi.com/?i=$imdbId&h=600&apikey=${BuildConfig.OMDB_API_KEY}"

        return movie
    }*/
}

@Entity(tableName = "RATING_NOT_FOUND", indices = arrayOf(Index("TITLE")))
data class RatingNotFound(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    @ColumnInfo(name = "TITLE")
    val title: String,

    @ColumnInfo(name = "YEAR")
    val year: String = "",

    @ColumnInfo(name = "TIMESTAMP")
    val timestamp: Long
)