package com.fenchtose.movieratings.model.entity

import com.fenchtose.movieratings.model.db.entity.Rating
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OmdbMovie(
    @Json(name = "ImdbID")
    val imdbId: String,
    @Json(name = "Type")
    val type: String,
    @Json(name = "Title")
    val title: String,
    @Json(name = "Year")
    val year: String,
    @Json(name = "Released")
    val released: String,
    @Json(name = "Genre")
    val genre: String,
    @Json(name = "Director")
    val director: String,
    @Json(name = "Writer")
    val writers: String,
    @Json(name = "Actors")
    val actors: String,
    @Json(name = "Plot")
    val plot: String,
    @Json(name = "Poster")
    val poster: String,
    @Json(name = "Ratings")
    val ratings: List<OmdbRating>,
    @Json(name = "totalSeasons")
    val seasons: Int = -1, // for tv shows
    @Json(name = "imdbRating")
    val imdbRating: String = "",
    @Json(name="imdbVotes")
    val imdbVotes: String = "",
    @Json(name = "Website")
    val website: String = "",
    @Json(name = "Runtime")
    val runtime: String = "",
    @Json(name = "Language")
    val language: String = "",
    @Json(name = "Rated")
    val rated: String = "",
    @Json(name = "Country")
    val country: String = "",
    @Json(name = "Awards")
    val awards: String = "",
    @Json(name = "Production")
    val production: String = ""
) {
    fun convert(): Movie {
        return Movie(
                imdbId = imdbId,
                title = title,
                year = year,
                type = type,
                poster = poster,

                // misc
                genre = genre,
                director = director,
                released = released,
                actors = actors,
                writers = writers,
                plot = plot,
                imdbRating = "$imdbRating/10",
                seasons = seasons,
                ratings = ratings,

                // unused
                rated = rated,
                runtime = runtime,
                language = language,
                website = website,
                imdbVotes = imdbVotes,
                country = country,
                production = production,
                awards = awards
        )
    }
}

@JsonClass(generateAdapter = true)
data class OmdbRating(
    @Json(name = "Source")
    val source: String,
    @Json(name = "Value")
    val rating: String
) {
    fun convert(): Rating {
        return Rating(source, rating)
    }
}

