package com.fenchtose.movieratings.model.entity

import com.fenchtose.movieratings.util.add


data class MovieCollection(
    val id: Long,
    val name: String,
    val movies: List<Movie>
) {
    companion object {
        fun invalid(): MovieCollection {
            return MovieCollection(-1, "", listOf())
        }
    }

    fun addMovie(movie: Movie): MovieCollection {
        if (movies.hasMovie(movie) == -1) {
            return copy(movies = movies.add(movie))
        }

        return this
    }

    fun removeMovie(movie: Movie): MovieCollection {
        if (movies.hasMovie(movie) != -1) {
            return copy(movies = movies.remove(movie))
        }

        return this
    }
}

fun com.fenchtose.movieratings.model.db.entity.MovieCollection.convert(): MovieCollection {
    return MovieCollection(
            id = id,
            name = name,
            movies = listOf()
    )
}