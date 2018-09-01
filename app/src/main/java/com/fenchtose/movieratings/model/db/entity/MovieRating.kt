package com.fenchtose.movieratings.model.db.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.fenchtose.movieratings.BuildConfig
import com.fenchtose.movieratings.model.entity.OmdbMovie
import com.fenchtose.movieratings.util.FixTitleUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "MOVIE_RATINGS", indices = arrayOf(Index("IMDBID", unique = true)))
data class MovieRating(
        @PrimaryKey
        @ColumnInfo(name = "IMDBID")
        @Json(name="id")
        val imdbId: String,

        @ColumnInfo(name = "IMDB_RATING")
        @Json(name="rating")
        val rating: Float,

        @ColumnInfo(name = "IMDB_VOTES")
        @Json(name="votes")
        val votes: Int,

        @ColumnInfo(name = "TITLE")
        @Json(name="title")
        val title: String,

        @ColumnInfo(name = "TITLE_TYPE")
        @Json(name="type")
        val type: String,

        @ColumnInfo(name = "TRANSLATED_TITLE")
        @Json(name = "translated_title")
        val translatedTitle: String,

        @ColumnInfo(name = "START_YEAR")
        @Json(name="start_year")
        val startYear: Int,

        @ColumnInfo(name = "END_YEAR")
        @Json(name="end_year")
        val endYear: Int = -1,

        @Transient
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

    companion object {
        fun empty(): MovieRating {
            return MovieRating("", -1f, -1, "", "", "", -1, -1, -1)
        }

        fun fromMovie(movie: OmdbMovie): MovieRating {

            movie.ratings.firstOrNull {
                it.source == "Internet Movie Database"
            }?.let {
                var startYear = -1
                var endYear = -1

                val years = FixTitleUtils.splitYears(movie.year)
                if (years.isNotEmpty()) {
                    startYear = years[0].toIntOrNull() ?: -1
                }
                if (years.size > 1) {
                    endYear = years[1].toIntOrNull() ?: -1
                }

                val rating = MovieRating(
                        imdbId = movie.imdbId,
                        type = movie.type,
                        title = movie.title,
                        rating = it.rating.split("/").firstOrNull()?.toFloatOrNull() ?:0f,
                        votes = movie.imdbVotes.replace(",","").toIntOrNull() ?: -1,
                        startYear = startYear,
                        endYear = endYear,
                        translatedTitle = ""
                )

                return rating
            }

            return empty()
        }
    }

    fun toMovie(): Movie {
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
    }
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