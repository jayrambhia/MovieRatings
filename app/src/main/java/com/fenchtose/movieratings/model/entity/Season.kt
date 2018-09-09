package com.fenchtose.movieratings.model.entity

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Season(
    @Json(name = "Title")
    val title: String,
    @Json(name = "Season")
    val season: Int,
    @Json(name = "totalSeasons")
    val totalSeasons: Int,
    @Json(name="Episodes")
    val episodes: List<Episode>,
    @Transient
    val seriesId: String = "")

@JsonClass(generateAdapter = true)
data class Episode(
    @Json(name = "Title")
    val title: String,
    @Json(name = "Released")
    val released: String,
    @Json(name="Episode")
    val episode: Int,
    @Json(name="imdbRating")
    val imdbRating: String,
    @Json(name="imdbID")
    val imdbId: String,

    @Transient
    val seriesId: String = "",
    @Transient
    val season: Int = -1


) {
    fun convert(): com.fenchtose.movieratings.model.db.entity.Episode {
        val dbEpisode = com.fenchtose.movieratings.model.db.entity.Episode()
        dbEpisode.seriesId = seriesId
        dbEpisode.season = season
        dbEpisode.imdbId = imdbId
        dbEpisode.released = released
        dbEpisode.title = title
        dbEpisode.imdbRating = imdbRating
        dbEpisode.episode = episode

        return dbEpisode
    }

    fun update(seriesId: String, season: Int): Episode {
        return copy(seriesId = seriesId, season = season)
    }
}

fun com.fenchtose.movieratings.model.db.entity.Episode.convert(): Episode {
    return Episode(
            title = title,
            released = released,
            episode = episode,
            imdbRating = imdbRating,
            imdbId = imdbId,
            seriesId = seriesId,
            season = season
    )
}

fun List<com.fenchtose.movieratings.model.db.entity.Episode>.convert(series: Movie, season: Int): Season {
    return Season(
            title = series.title,
            season = season,
            totalSeasons = series.seasons,
            episodes = map { it.convert() },
            seriesId = series.imdbId
    )
}