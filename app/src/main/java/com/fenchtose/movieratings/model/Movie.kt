package com.fenchtose.movieratings.model

import android.arch.persistence.room.*
import com.fenchtose.movieratings.model.db.MovieTypeConverter2
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "MOVIES", indices = arrayOf(Index("IMDBID", unique = true)))
@TypeConverters(value = MovieTypeConverter2::class)
class Movie {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "TITLE")
    @SerializedName("Title")
    var title: String = ""

    @ColumnInfo(name = "POSTER")
    @SerializedName("Poster")
    var poster: String = ""

    @ColumnInfo(name = "RATINGS")
    @SerializedName("Ratings")
    var ratings: ArrayList<Rating> = ArrayList()

    @ColumnInfo(name = "TYPE")
    @SerializedName("Type")
    var type: String = ""

    @ColumnInfo(name = "IMDBID")
    @SerializedName("imdbID")
    var imdbId: String = ""

    @ColumnInfo(name = "YEAR")
    @SerializedName("Year")
    var year: String = ""

    @ColumnInfo(name = "RATED")
    @SerializedName("Rated")
    var rated: String = ""

    @ColumnInfo(name = "RELEASED")
    @SerializedName("Released")
    var released: String = ""

    @ColumnInfo(name = "RUNTIME")
    @SerializedName("Runtime")
    var runtime: String = ""

    @ColumnInfo(name = "GENRE")
    @SerializedName("Genre")
    var genre: String = ""

    @ColumnInfo(name = "DIRECTOR")
    @SerializedName("Director")
    var director: String = ""

    @ColumnInfo(name = "WRITERS")
    @SerializedName("Writer", alternate = ["Writers"])
    var writers: String = ""

    @ColumnInfo(name = "ACTORS")
    @SerializedName("Actors")
    var actors: String = ""

    @ColumnInfo(name = "PLOT")
    @SerializedName("Plot")
    var plot: String = ""

    @ColumnInfo(name = "LANGUAGE")
    @SerializedName("Language")
    var language: String = ""

    @ColumnInfo(name = "COUNTRY")
    @SerializedName("Country")
    var country: String = ""

    @ColumnInfo(name = "AWARDS")
    @SerializedName("Awards")
    var awards: String = ""

    @ColumnInfo(name = "IMDBVOTES")
    @SerializedName("imdbVotes")
    var imdbVotes: String = ""

    @ColumnInfo(name = "PRODUCTION")
    @SerializedName("Production")
    var production: String = ""

    @ColumnInfo(name = "WEBSITE")
    @SerializedName("Website")
    var website: String = ""

    @Ignore
    var liked: Boolean = false

    override fun toString(): String {
        return "Movie(id='$id', title='$title', poster='$poster', ratings=$ratings)"
    }

    fun isComplete(): Boolean {
        // Title, Year, ImdbId, Type and Poster are available in search. Let's check if at least 3 are available.
        if (checkValid(title, year, imdbId, type, poster)) {
            // check for others
            return checkValid(rated, released, runtime, genre, language, actors, plot, website, writers)
        }

        return false
    }

    private fun checkValid(vararg fields: String?): Boolean {
        return fields.filter {
            !it.isNullOrEmpty()
        }.size > fields.size/2
    }

    companion object {
        fun empty() : Movie {
            val movie = Movie()
            movie.id = -1
            return movie
        }
    }
}