package com.fenchtose.movieratings.model.entity

import com.fenchtose.movieratings.model.db.entity.MovieCollection
import com.fenchtose.movieratings.util.replace

data class Movie(
    // essentials

    val imdbId: String,
    val title: String,
    val year: String,
    val type: String,
    val poster: String,

    // misc
    val genre: String = "",
    val director: String = "",
    val released: String = "",
    val actors: String = "",
    val writers: String = "",
    val plot: String = "",
    val imdbRating: String = "",
    val seasons: Int = -1,
    val ratings: List<OmdbRating> = listOf(),

    // unused
    val rated: String = "",
    val runtime: String = "",
    val language: String = "",
    val website: String = "",
    val imdbVotes: String = "",
    val country: String = "",
    val production: String = "",
    val awards: String = "",

    // user pref
    val liked: Boolean = false,
    val collections: List<MovieCollection> = listOf(),
    val preferences: AppliedPreferences = AppliedPreferences()
) {

    data class AppliedPreferences(val liked: Boolean = false, val collections: Boolean = false) {
        fun checkValid(): Boolean {
            return liked && collections
        }
    }

    fun like(liked: Boolean): Movie {
        return copy(liked = liked, preferences = preferences.copy(liked = true))
    }

    private fun checkValidBase(): Boolean {
        return checkValid(title, year, imdbId, type, poster)
    }

    private fun checkValidExtras(): Boolean {
        return checkValid(rated, released, runtime, genre, language, actors, plot, website, writers)
    }

    fun isComplete(check: Check): Boolean {
        return when(check) {
            Check.BASE -> checkValidBase()
            Check.EXTRA -> checkValidBase() && checkValidExtras()
//            Check.LIKED -> checkValidBase() && checkValidExtras() && appliedPreferences.liked
            Check.USER_PREFERENCES -> checkValidBase() && checkValidExtras() && preferences.checkValid()
        }
    }

    private fun checkValid(vararg fields: String?): Boolean {
        return fields.filter {
            !it.isNullOrEmpty()
        }.size > fields.size/2
    }

    enum class Check {
        BASE,
        EXTRA,
//        LIKED,
        USER_PREFERENCES
    }

    companion object {
        fun invalid(): Movie {
            return Movie("", "", "", "", "")
        }

        fun withId(imdbId: String): Movie {
            return Movie(imdbId, "", "", "", "")
        }
    }

    fun convert(): com.fenchtose.movieratings.model.db.entity.Movie {
        val dbMovie = com.fenchtose.movieratings.model.db.entity.Movie()
        dbMovie.imdbId = imdbId
        dbMovie.title = title
        dbMovie.poster = poster
        dbMovie.ratings = ArrayList(ratings.map { it.convert() })
        dbMovie.type = type
        dbMovie.year = year
        dbMovie.rated = rated
        dbMovie.released = released
        dbMovie.runtime = runtime
        dbMovie.genre = genre
        dbMovie.director = director
        dbMovie.writers = writers
        dbMovie.actors = actors
        dbMovie.plot = plot
        dbMovie.language = language
        dbMovie.country = country
        dbMovie.awards = awards
        dbMovie.imdbVotes = imdbVotes
        dbMovie.production = production
        dbMovie.website = website
        dbMovie.totalSeasons = seasons

        return dbMovie
    }
}

fun List<Movie>.hasMovie(movie: Movie): Int {
    forEachIndexed {
        index, m -> if (m.imdbId == movie.imdbId) { return index }
    }

    return -1
}

fun List<Movie>.updateMovie(movie: Movie): List<Movie> {
    val index = hasMovie(movie)
    if (index != -1) {
        return this.replace(index, movie)
    }

    return this
}

fun List<Movie>.remove(movie: Movie): List<Movie> {
    val index = hasMovie(movie)
    if (index != -1) {
        val updated = toMutableList()
        updated.removeAt(index)
        return updated
    }

    return this
}

fun com.fenchtose.movieratings.model.db.entity.Movie.convert(): Movie {
    return Movie(
            title = title,
            poster = poster,
            ratings = ratings.map { OmdbRating(it.source, it.value) },
            type = type,
            imdbId = imdbId,
            year = year,
            rated = rated,
            released = released,
            runtime = runtime,
            genre = genre,
            director = director,
            writers = writers,
            actors = actors,
            plot = plot,
            language = language,
            country = country,
            awards = awards,
            imdbVotes = imdbVotes,
            production = production,
            website = website,
            seasons = totalSeasons
    )
}

fun List<com.fenchtose.movieratings.model.db.entity.Movie>.convert(): List<Movie> {
    return map {it.convert()}
}