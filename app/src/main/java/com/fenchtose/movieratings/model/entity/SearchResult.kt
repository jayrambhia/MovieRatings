package com.fenchtose.movieratings.model.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SearchResult(
    @Json(name="Search")
    val results: List<SearchMovie> = listOf(),

    @Transient
    val movies: List<Movie> = listOf(),

    @Json(name="totalResults")
    val total: Int = 0,

    @Json(name="Response")
    val success: String = "",

    @Json(name = "Error")
    var error: String?) {

    fun convert(): SearchResult {
        return copy(movies = results.map { it.convert() })
    }
}


@JsonClass(generateAdapter = true)
data class SearchMovie(
    @Json(name="Title")
    val title: String,
    @Json(name="Year")
    val year: String,
    @Json(name="imdbID")
    val imdbId: String,
    @Json(name="Poster")
    val poster: String,
    @Json(name="Type")
    val type: String
) {
    fun convert(): Movie {
        return Movie(
                imdbId = imdbId,
                title = title,
                year = year,
                type = type,
                poster = poster
        )
    }
}