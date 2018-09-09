package com.fenchtose.movieratings.model.entity

import com.fenchtose.movieratings.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trending(
    @Json(name="trending")
    val movies: List<TrendingMovie>
)

@JsonClass(generateAdapter = true)
data class TrendingMovie(
    val id: String,
    val rating: Float,
    @Json(name="translated_title")
    val translatedTitle: String,
    val votes: Int,
    val title: String,
    val type: String,
    @Json(name="start_year")
    val startYear: Int,
    @Json(name="end_year")
    val endYear: Int?
) {
    fun convert(): Movie {
        val year = if (startYear != -1 && endYear != -1) {
            "$startYear - $endYear"
        } else {
            startYear.toString()
        }

        return Movie(
            imdbId = id,
            title = title,
            type = type,
            year = year,
            poster = "http://img.omdbapi.com/?i=$id&h=600&apikey=${BuildConfig.OMDB_API_KEY}",
            imdbVotes = votes.toString(),
            imdbRating = rating.toString()
        )
    }
}
