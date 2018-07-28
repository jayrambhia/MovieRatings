package com.fenchtose.movieratings.model.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import com.fenchtose.movieratings.util.FixTitleUtils
import com.google.gson.annotations.SerializedName

@Entity(tableName = "MOVIE_RATINGS", indices = arrayOf(Index("IMDBID", unique = true)))
class MovieRating {

    @PrimaryKey
    @ColumnInfo(name = "IMDBID")
    @SerializedName("id")
    var imdbId: String = ""

    @ColumnInfo(name = "IMDB_RATING")
    @SerializedName("rating")
    var rating: Float = 0f

    @ColumnInfo(name = "IMDB_VOTES")
    @SerializedName("votes")
    var votes: Int = 0

    @ColumnInfo(name = "TITLE")
    @SerializedName("title")
    var title: String = ""

    @ColumnInfo(name = "TITLE_TYPE")
    @SerializedName("type")
    var type: String = ""

    @ColumnInfo(name = "TRANSLATED_TITLE")
    @SerializedName("translated_title")
    var translatedTitle: String = ""

    @ColumnInfo(name = "START_YEAR")
    @SerializedName("start_year")
    var startYear: Int = -1

    @ColumnInfo(name = "END_YEAR")
    @SerializedName("end_year")
    var endYear: Int = -1

    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Int = -1

    fun fitsYear(year: Int): Boolean {
        if (year != -1) {
            if (startYear == -1 && endYear == -1) {
                return false
            }

            if (startYear == -1) {
                return endYear == year
            }

            if (endYear == -1) {
                return startYear == year
            }

            return year in startYear..endYear
        }

        return true
    }

    companion object {
        fun empty(): MovieRating {
            return MovieRating()
        }

        fun fromMovie(movie: Movie): MovieRating {
            val rating = MovieRating()
            movie.ratings.firstOrNull {
                it.source == "Internet Movie Database"
            }?.let {
                rating.imdbId = movie.imdbId
                rating.title = movie.title
                rating.type = movie.type

                val years = FixTitleUtils.splitYears(movie.year)
                if (years.isNotEmpty()) {
                    rating.startYear = years[0].toIntOrNull() ?: -1
                }
                if (years.size > 1) {
                    rating.endYear = years[1].toIntOrNull() ?: -1
                }

                rating.rating = it.value.split("/").firstOrNull()?.toFloatOrNull() ?:0f
                rating.votes = movie.imdbVotes.replace(",","").toIntOrNull() ?: -1
            }

            return rating
        }
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