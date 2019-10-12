package com.fenchtose.movieratings.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.fenchtose.movieratings.model.db.entity.MovieRating.Companion.ID
import com.fenchtose.movieratings.util.Constants

@Entity(tableName = "MOVIE_RATINGS", indices = arrayOf(Index(ID, unique = true)))
data class MovieRating(
        @PrimaryKey
        @ColumnInfo(name = ID)
        val imdbId: String,

        @ColumnInfo(name = RATING)
        val rating: Float,

        @ColumnInfo(name = VOTES)
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
        val timestamp: Int = -1,

        @ColumnInfo(name = "SOURCE")
        val source: String
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
                if (type == Constants.RATING_TYPE_SERIES) {
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
        // TODO change these column names
        const val ID = "IMDBID"
        const val RATING = "IMDB_RATING"
        const val VOTES = "IMDB_VOTES"
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